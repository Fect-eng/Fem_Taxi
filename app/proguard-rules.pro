#firebase
-keepattributes Signature
 -keepclassmembers class pe.com.android.femtaxi.models.** {
      *;
    }

-keep public class com.google.firebase.analytics.FirebaseAnalytics {
    public *;
}

-keep public class com.google.android.gms.measurement.AppMeasurement {
    public *;
}