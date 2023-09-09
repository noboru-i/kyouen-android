plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

//// https://docs.deploygate.com/docs/gradle-plugin
//apply plugin: "deploygate"
//
//// https://github.com/ben-manes/gradle-versions-plugin
//apply plugin: "com.github.ben-manes.versions"
//
//// https://firebase.google.com/docs/perf-mon/get-started-android
//apply plugin: "com.google.firebase.firebase-perf"
//
//// https://github.com/jlleitschuh/ktlint-gradle
//apply plugin: "org.jlleitschuh.gradle.ktlint"

android {
    namespace = "hm.orz.chaos114.android.tumekyouen"
    compileSdk = 33

    buildFeatures {
        // https://stackoverflow.com/a/76332575
        buildConfig = true

        dataBinding = true
    }

    defaultConfig {
        applicationId = "hm.orz.chaos114.android.tumekyouen"
        minSdk = 24
        targetSdk = 33
        versionCode = 25
        versionName = "0.12.0"
//        multiDexEnabled true

        javaCompileOptions {
            annotationProcessorOptions {
                compilerArgumentProviders(
                    RoomSchemaArgProvider(File(projectDir, "schemas"))
                )
            }
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("cert/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            storeFile = file("cert/release.keystore")
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = "chaos114"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
//    lintOptions {
//        abortOnError false
//        lintConfig file("lint.xml")
//    }
//    testOptions {
//        unitTests {
//            includeAndroidResources = true
//        }
//    }
//    compileOptions {
//        sourceCompatibility JavaVersion.VERSION_1_8
//        targetCompatibility JavaVersion.VERSION_1_8
//    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
}


dependencies {
    val coroutinesVersion = "1.6.4"
    val okhttpLibVersion = "4.9.0"
    val retrofitLibVersion = "2.9.0"
    val moshiLibVersion = "1.11.0"
//    val icepickVersion = "3.2.0"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${coroutinesVersion}")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation("com.google.android.gms:play-services-ads:22.3.0")

    // https://developer.android.com/jetpack/androidx/releases/lifecycle (ViewModel and LiveData)
    val lifecycle_version = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycle_version")

    // room
    val room_version = "2.5.2"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-rxjava2:$room_version")
    androidTestImplementation("android.arch.persistence.room:testing")

//    // Twitter SDK
//    implementation("com.twitter.sdk.android:twitter-core:3.3.0")

    // Timber
    implementation("com.jakewharton.timber:timber:4.7.1")

    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:${okhttpLibVersion}")
    implementation("com.squareup.okhttp3:logging-interceptor:${okhttpLibVersion}")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:${okhttpLibVersion}")
    implementation("com.squareup.retrofit2:retrofit:${retrofitLibVersion}")
    implementation("com.squareup.retrofit2:adapter-rxjava2:${retrofitLibVersion}")
    implementation("com.squareup.retrofit2:converter-gson:${retrofitLibVersion}")
    implementation("com.squareup.retrofit2:converter-moshi:${retrofitLibVersion}")
    implementation("io.reactivex.rxjava2:rxjava:2.2.20")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.uber.autodispose:autodispose-ktx:1.2.0")
    implementation("com.uber.autodispose:autodispose-android-ktx:1.2.0")
    implementation("com.uber.autodispose:autodispose-android-archcomponents-ktx:1.2.0")
    implementation("com.squareup.moshi:moshi:${moshiLibVersion}")
    implementation("com.squareup.moshi:moshi-kotlin:${moshiLibVersion}")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:${moshiLibVersion}")

//    // Icepick
//    implementation("frankiesardo:icepick:${icepickVersion}")
//    kapt("frankiesardo:icepick-processor:${icepickVersion}")

    // Dagger2
    val dagger_version = "2.30.1"
    implementation("com.google.dagger:dagger:$dagger_version")
    implementation("com.google.dagger:dagger-android:$dagger_version")
    implementation("com.google.dagger:dagger-android-support:$dagger_version")
    kapt("com.google.dagger:dagger-compiler:$dagger_version")
    kapt("com.google.dagger:dagger-android-processor:$dagger_version")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.4")
    testImplementation("org.robolectric:shadows-multidex:4.4")
}

//ktlint {
//    version = "0.40.0"
//    android = true
//    reporters {
//        reporter "checkstyle"
//    }
//    ignoreFailures = true
//    filter {
//        exclude("**/generated/**")
//        exclude("**/network/models/**")
//        include("**/java/**")
//    }
//}
//
//detekt {
//    version = "1.0.0-RC16"
//    input = files("src/main/java")
//    config = files("$rootDir/detekt.yml")
//}
//tasks.withType(io.gitlab.arturbosch.detekt.Detekt).configureEach {
//    exclude("**/network/models/**")
//}

// https://qiita.com/Nabe1216/items/322caa7acf11dbe032ca
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
    .configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }

// https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=ja#kotlin_1
class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String> {
        return listOf("-Aroom.schemaLocation=${schemaDir.path}")
    }
}
