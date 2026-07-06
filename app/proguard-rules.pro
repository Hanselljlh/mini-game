# Pocket Arcade Offline — R8 / ProGuard rules
#
# The app is reflection-free pure Kotlin + Jetpack Compose. Compose, AndroidX,
# and Play Billing all ship their own consumer rules, so almost nothing extra
# is needed. Keep our data models intact just in case R8's optimizer is
# over-eager with classes only referenced through Compose state.

-keep class net.sclan.minigames.data.** { *; }

# Keep enum values() / valueOf() used by difficulty selectors.
-keepclassmembers enum net.sclan.minigames.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Standard: strip Android logging in release (optional, harmless).
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}
