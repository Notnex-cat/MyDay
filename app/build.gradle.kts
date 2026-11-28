plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.notnex.myday"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.notnex.myday"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    // В приложении два языка. Без фильтра в сборку попадают переводы библиотек на сотню языков.
    androidResources {
        localeFilters += listOf("en", "ru")
    }

    // Исходники .proto и тексты лицензий библиотек в работе приложения не участвуют.
    packaging {
        resources {
            excludes += setOf(
                "**/*.proto",
                "META-INF/**/LICENSE.txt",
                "META-INF/{AL2.0,LGPL2.1}",
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }

    buildFeatures {
        compose = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // collectAsStateWithLifecycle: поток не собирается, пока экран не виден
    implementation(libs.androidx.lifecycle.runtime.compose)
    // ProcessLifecycleOwner: синхронизация идёт, только пока приложение на экране
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.animation)
    // Только базовый набор иконок; недостающие шесть лежат в ui/theme/AppIcons.kt.
    // Полный набор (icons-extended) раздувал debug-сборку на десятки мегабайт.
    implementation(libs.androidx.material.icons.core)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // настройки пользователя: ключ — значение
    implementation(libs.androidx.datastore.preferences)

    // шифрование секретов (ключ нейросети) ключом из Android Keystore
    implementation(libs.tink.android)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    //firebase
    implementation(libs.firebase.auth)
    // Вход через Google: Credential Manager вместо устаревшего One Tap.
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)

    //coil
    implementation(libs.coil.compose)

    //okhttp
    implementation(libs.okhttp)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
