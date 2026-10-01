package com.fabsys.notipe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Conectando a Supabase...", Toast.LENGTH_SHORT).show()

                // Llamamos a la función de validación
                validarCredenciales(username, password)

            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Nueva función que se comunica con Retrofit
    private fun validarCredenciales(user: String, pass: String) {
        val call = RetrofitClient.api.validarLogin(
            apiKey = RetrofitClient.SUPABASE_KEY,
            authToken = "Bearer ${RetrofitClient.SUPABASE_KEY}",
            username = "eq.$user",
            password = "eq.$pass"
        )

        call.enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val usuarioLogueado = response.body()!![0]

                    if (!usuarioLogueado.estado_activo) {
                        Toast.makeText(this@LoginActivity, "Usuario desactivado", Toast.LENGTH_SHORT).show()
                        return
                    }

                    Toast.makeText(this@LoginActivity, "Bienvenido: ${usuarioLogueado.rol}", Toast.LENGTH_SHORT).show()

                    // Pasamos a la pantalla principal
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}