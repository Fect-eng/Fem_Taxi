package pe.com.android.femtaxi.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({StatusTrip.TRIP,
        StatusTrip.BOARDING,
        StatusTrip.CANCEL,
        StatusTrip.FINISH})
@Retention(RetentionPolicy.SOURCE)
public @interface StatusTrip {
    int TRIP = 0;
    int BOARDING = 1;
    int CANCEL = 2;
    int FINISH = 3;
}