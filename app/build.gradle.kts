plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.fabsys.notipe"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.fabsys.notipe"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // Material Design (Para el menú lateral, sombras y tarjetas modernas)
    implementation("com.google.android.material:material:1.11.0")

    // Navegación (Para moverse entre el Dashboard y Settings sin recargar la pantalla)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Gráficos (Para las curvas de ingresos en el Dashboard del Administrador)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Cliente HTTP (Para conectarnos a Supabase y verificar los roles de usuario)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}