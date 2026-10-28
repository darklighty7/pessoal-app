plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    kotlin("kapt")
}

android {
    namespace = "dev.pedro.pessoal"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.pedro.pessoal"
        minSdk = 26
        targetSdk = 35
        versionCode = 19
        versionName = "1.12.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        // Para medir desempenho de verdade e usar no celular pessoal: release com R8 ligado, assinado com a
        // chave de debug só para poder instalar. O APK de debug roda o Compose bem mais devagar e não
        // representa o app final. Instalar com: ./gradlew installOtimizado
        create("otimizado") {
            initWith(getByName("release"))
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // O firebase-auth arrasta uma versão antiga do androidx.fragment, e o lint só do build de
    // release reprova por isso: registerForActivityResult exige fragment 1.3.0 ou mais novo.
    // O app não usa fragment nenhum — é uma Activity só, tudo em Compose. A restrição eleva o
    // piso de quem puxar a dependência, sem acrescentá-la ao app.
    constraints {
        implementation(libs.androidx.fragment)
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    kapt(libs.androidx.room.compiler)

    // Firebase: login por e-mail/senha e perfil do personagem na nuvem.
    // O BoM manda nas versões; as bibliotecas abaixo ficam sem versão de propósito.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
