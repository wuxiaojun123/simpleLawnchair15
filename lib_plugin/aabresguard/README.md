# AabResGuard local plugin

This folder contains the local AabResGuard Gradle plugin jars and shared configuration.

To copy this integration to another branch, copy this whole folder and keep these entries in the root build.gradle:

```gradle
buildscript {
    dependencies {
        classpath files(
            "${rootDir}/lib_plugin/aabresguard/aabResGuard-plugin-1.0.3.14_gp8.jar",
            "${rootDir}/lib_plugin/aabresguard/aabresguard-core-1.0.3.1.jar",
        )
    }
}

apply from: rootProject.file("lib_plugin/aabresguard/aabresguard.gradle")
```

Run the generated task for the target bundle variant, for example:

```powershell
.\gradlew.bat aabresguardLawnWithQuickstepPlayRelease
```