# ActivityGuard local integration

This folder contains the shared ActivityGuard Gradle configuration.

The plugin classpath is declared in the root build.gradle because ActivityGuard is an old-style Gradle plugin:

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath "com.github.denglongfei:activityGuard:1.3.0"
    }
}
```

The app applies the reusable script from the root build.gradle:

```gradle
apply from: rootProject.file("lib_plugin/activity_guard/activity_guard.gradle")
```

## BouncyCastle unsigned jars

ActivityGuard reads and rewrites every input jar with `JarFile`. Signed BouncyCastle jars can fail with `SHA-256 digest error`, so `lib:library_pdf` excludes the transitive signed BouncyCastle jars from pdfbox-android and uses the unsigned copies in `bouncycastle/`.
