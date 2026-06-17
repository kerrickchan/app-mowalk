# Manifest-registered components (referenced by name in AndroidManifest.xml)
-keep class com.mowalk.app.MoWalkApplication { *; }
-keep class com.mowalk.app.ui.main.MainActivity { *; }
-keep class com.mowalk.app.service.StepCounterService { *; }
-keep class com.mowalk.app.service.BootReceiver { *; }

# Room entities, DAOs, and database
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * implements androidx.room.Dao {
    *;
}
-keep class com.mowalk.app.data.local.MoWalkDatabase { *; }
-keep class com.mowalk.app.data.local.MoWalkDatabase_Impl { *; }

# Keep entity companion objects and data classes for Room
-keepclassmembers class **$$Companion {
    *;
}
-keepclassmembers class com.mowalk.app.data.local.** {
    <fields>;
    <init>(...);
}

# ViewModels (reflected by lifecycle-viewmodel)
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Compose runtime
-dontwarn androidx.compose.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# MPAndroidChart (JitPack library, uses reflection)
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Desugaring - keep java.time and other desugared platform classes
-keep class j$.time.** { *; }
-dontwarn j$.time.**

# Kotlin metadata (required for reflection)
-keep class kotlin.Metadata { *; }

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep R classes (resource IDs)
-keep class **.R$* { *; }
