plugins {
  alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.realestate"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.realestate"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      viewBinding = true
      compose = false
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}


dependencies {
  // Core Android dependencies
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.constraintlayout:constraintlayout:2.2.0")
  implementation("androidx.recyclerview:recyclerview:1.3.2")
  implementation("androidx.cardview:cardview:1.0.0")

  // Networking, HTTP Client & Serialization
  implementation(libs.retrofit)
  implementation("com.squareup.retrofit2:converter-gson:2.11.0")
  implementation("com.google.code.gson:gson:2.10.1")
  implementation(libs.okhttp)
  implementation(libs.okhttp.logging.interceptor)

  // Image Loading (Glide for Java XML Views)
  implementation("com.github.bumptech.glide:glide:4.16.0")
  annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

  // WebSockets Chat Client
  implementation(libs.socket.io.client)

  // SwipeRefreshLayout
  implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

  // ViewPager2
  implementation("androidx.viewpager2:viewpager2:1.1.0")
}
