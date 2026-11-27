# Room 2.5.1 and WorkManager 2.9.0 lose these constructors under AGP 9.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keepclassmembers class * extends androidx.work.InputMerger { <init>(); }

# Gson resolves enum values by reflecting over the enum's constant fields. Without this, R8 drops the
# fields of constants not referenced in code (e.g. HealthRecord.STEPS) and Gson parses them as null.
-keepclassmembers enum com.outsystems.plugins.healthfitness.** { <fields>; }
