# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/anacrethus/Android/Sdk/tools/proguard/proguard-android.txt

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *

# Keep Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Keep models
-keep class com.example.smarthome.models.** { *; }
