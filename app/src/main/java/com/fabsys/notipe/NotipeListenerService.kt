package com.fabsys.notipe

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class NotipeListenerService : NotificationListenerService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val resultado = tts?.setLanguage(Locale("es", "PE")) ?: tts?.setLanguage(Locale("es", "ES"))
            if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("NOTIPE_YAPE", "❌ Error: Idioma español no soportado para TTS.")
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val paquete = sbn.packageName ?: return

        if (paquete == "com.bcp.innovacxion.yapeapp") {
            val extras = sbn.notification.extras
            val titulo = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val texto = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            if (titulo.contains("Confirmación de Pago", ignoreCase = true) &&
                texto.contains("te envió un pago por S/")) {

                val regex = "^(.*?) te envi[óo] un pago por S/\\s*([\\d.,]+)".toRegex(RegexOption.IGNORE_CASE)
                val matchResult = regex.find(texto)

                val regexCodigo = "seguridad es:\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE)
                val codigoMatch = regexCodigo.find(texto)
                val codigoYape = codigoMatch?.groupValues?.get(1) ?: "000"

                if (matchResult != null) {
                    val remitente = matchResult.groupValues[1].trim()
                    val montoTexto = matchResult.groupValues[2].replace(",", "").trimEnd('.')
                    val monto = montoTexto.toDoubleOrNull() ?: 0.0

                    Log.d("NOTIPE_YAPE", "💰 ¡BINGO! Enviando S/ $monto de $remitente a Supabase...")

                    // --- TRADUCCIÓN INTELIGENTE PARA LA VOZ ---
                    val nombreHablado = formatearNombreParaVoz(remitente)
                    val montoHablado = formatearMontoParaVoz(montoTexto)

                    val mensajeVoz = "Llegó un yape de $montoHablado, de $nombreHablado"
                    tts?.speak(mensajeVoz, TextToSpeech.QUEUE_ADD, null, null)

                    // --- GUARDADO EN BASE DE DATOS ---
                    // --- GUARDADO EN BASE DE DATOS (CON SUCURSAL) ---
                    // 1. Obtenemos el ID único del celular
                    val androidId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)

                    // 2. Le preguntamos a Supabase a qué tienda pertenece este celular
                    RetrofitClient.api.obtenerSucursalPorDispositivo(
                        RetrofitClient.SUPABASE_KEY,
                        "Bearer ${RetrofitClient.SUPABASE_KEY}",
                        "eq.$androidId"
                    ).enqueue(object : Callback<List<Sucursal>> {
                        override fun onResponse(call: Call<List<Sucursal>>, response: Response<List<Sucursal>>) {
                            val sucursales = response.body()
                            // Si encontramos una tienda con este dispositivo, extraemos su ID. Si no, queda nulo.
                            val idSucursalEncontrada = if (!sucursales.isNullOrEmpty()) sucursales[0].id else null

                            val nuevoPago = PagoYape(
                                nombre_remitente = remitente,
                                monto = monto,
                                codigo_yape = codigoYape,
                                sucursal_id = idSucursalEncontrada // ¡Aquí enlazamos el pago a la tienda!
                            )

                            // 3. Ahora sí, enviamos el pago completo a la tabla pagos_yape
                            RetrofitClient.api.registrarPagoYape(
                                RetrofitClient.SUPABASE_KEY,
                                "Bearer ${RetrofitClient.SUPABASE_KEY}",
                                nuevoPago
                            ).enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, responsePago: Response<Void>) {
                                    if (responsePago.isSuccessful) {
                                        Log.d("NOTIPE_YAPE", "✅ ¡PAGO VINCULADO Y GUARDADO!")
                                    }
                                }
                                override fun onFailure(call: Call<Void>, t: Throwable) {}
                            })
                        }

                        override fun onFailure(call: Call<List<Sucursal>>, t: Throwable) {
                            Log.e("NOTIPE_YAPE", "❌ Error al buscar la sucursal: ${t.message}")
                        }
                    })
                }
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    // ==========================================
    // FUNCIONES INTELIGENTES PARA LENGUAJE NATURAL
    // ==========================================

    private fun formatearNombreParaVoz(nombreCompleto: String): String {
        // Corta el texto en el primer espacio (toma solo el primer nombre) y elimina asteriscos
        val primerNombre = nombreCompleto.split(" ")[0]
        return primerNombre.replace("*", "").trim()
    }

    private fun formatearMontoParaVoz(montoTexto: String): String {
        val partes = montoTexto.split(".")
        val soles = if (partes.isNotEmpty() && partes[0].isNotEmpty()) partes[0].toInt() else 0

        var centimos = 0
        if (partes.size > 1) {
            val decimales = partes[1]
            // Si llega "1" (de 0.1) se convierte en 10. Si llega "20", se queda en 20.
            centimos = if (decimales.length == 1) "${decimales}0".toInt() else decimales.take(2).toInt()
        }

        var textoFinal = ""

        // Lógica de Soles
        if (soles == 1) {
            textoFinal += "un sol"
        } else if (soles > 1) {
            textoFinal += "$soles soles"
        }

        // Conector
        if (soles > 0 && centimos > 0) {
            textoFinal += " con "
        }

        // Lógica de Céntimos
        if (centimos > 0) {
            textoFinal += "$centimos céntimos"
        }

        if (soles == 0 && centimos == 0) {
            textoFinal = "cero soles"
        }

        return textoFinal
    }
}