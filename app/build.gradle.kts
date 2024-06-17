import com.android.build.gradle.internal.tasks.databinding.DataBindingGenBaseClassesTask
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool
import java.util.Properties

buildscript {
    File(projectDir, "google-services.json").let { googleServicesFile ->
        check(googleServicesFile.exists() && googleServicesFile.length() > 0) {
            "Inject [$googleServicesFile] to your project"
        }
    }
}

plugins {
    fun addPlugin(dependency: Provider<PluginDependency>) = id(dependency.get().pluginId)
    addPlugin(libs.plugins.android.application)
    addPlugin(libs.plugins.kotlin.android)
    addPlugin(libs.plugins.kotlin.ksp)
    addPlugin(libs.plugins.kotlin.parcelize)
    addPlugin(libs.plugins.dagger.hilt)
    addPlugin(libs.plugins.google.services)
    addPlugin(libs.plugins.navigation.safeargs)
}

android {
    namespace = libs.versions.android.applicationId.get()

    signingConfigs {
        getByName("debug") {
            storeFile = project.file("keystore/debug-keystore.jks")
            storePassword = "androiddebug"
            keyAlias = "androiddebug"
            keyPassword = "androiddebug"
        }
        create("release") {
            storeFile = File(getKeystoreProperty("storeFile"))
            storePassword = getKeystoreProperty("storePassword")
            keyAlias = getKeystoreProperty("keyAlias")
            keyPassword = getKeystoreProperty("keyPassword")
        }
    }

    defaultConfig {
        applicationId = libs.versions.android.applicationId.get()
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        versionCode = libs.versions.android.versionCode.get().toInt()
        versionName = libs.versions.android.versionName.get()

        val appDomain = "aleksandrovych.purchase.pal.com"
        manifestPlaceholders["appDomain"] = appDomain
        buildConfigField("String", "APP_DOMAIN", "\"$appDomain\"")
        buildConfigField("String", "FIREBASE_DEEPLINK_DOMAIN", "\"${getFirebaseProperty("deeplinkDomain")}\"")
        buildConfigField("String", "REALTIME_DB_LOCATION", "\"${getFirebaseProperty("realtimeDbDomain")}\"")
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    }
    kotlinOptions {
        jvmTarget = libs.versions.java.get()
    }
}

androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            project.tasks.getByName("ksp" + variant.name.capitalized() + "Kotlin") {
                val dataBindingTask = project
                    .tasks
                    .getByName("dataBindingGenBaseClasses" + variant.name.capitalized())
                        as DataBindingGenBaseClassesTask
                (this as AbstractKotlinCompileTool<*>).setSource(dataBindingTask.sourceOutFolder)
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.google.gson)
    implementation(libs.google.material)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.firebase.dynamicLinks.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}

fun loadPropertiesFile(file: File): Properties? {
    return Properties().runCatching {
        load(file.reader())
        this
    }.getOrDefault(null)
}

fun getKeystoreProperty(name: String): String {
    val properties = loadPropertiesFile(rootProject.file("keystore.properties"))
    return properties?.get(name) as? String ?: run {
        logger.warn("Keystore property [$name] is not available. android.signingConfigs.debug.$name is used")
        val debugSigningConfig = android.signingConfigs.getByName("debug")

        return@run debugSigningConfig::class
            .java
            .declaredFields
            .first { field ->
                field.isAccessible = true
                field.name.contains(name)
            }
            .get(debugSigningConfig)
            .toString()
    }
}

fun getFirebaseProperty(name: String): String {
    val file = rootProject.file("firebase.properties")
    return checkNotNull(loadPropertiesFile(file)?.get(name) as? String) { "Unable get property [$name] from $file" }
}