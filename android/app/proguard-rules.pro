# Room entities and DAOs
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * implements androidx.room.Dao {
    *;
}

# Keep entity companion objects for factory methods
-keepclassmembers class **$$Companion {
    *;
}
