# Base ProGuard
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

-keep class go.** { *; }
-keep class com.hiddify.core.libbox.** { *; }

# Hilt / Dagger
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.lifecycle.HiltViewModel class *
-keep class dagger.hilt.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.Query *;
    @androidx.room.Insert *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
}

# DataStore
-keep class com.yasliks.database.data.SettingsData { *; }
-keep class com.yas.database.data.ServerDTO { *; }
-keep class com.yas.database.data.AppDTO { *; }