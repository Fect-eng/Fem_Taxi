package pe.com.android.femtaxi.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ServiceType.TAXI,
        ServiceType.INTRA_URBANO,
        ServiceType.DELIVERY,
        ServiceType.MESSAGING,
        ServiceType.CARGA,
        ServiceType.PET,
        ServiceType.FRIEND})
@Retention(RetentionPolicy.SOURCE)
public @interface ServiceType {
    int TAXI = 0;
    int INTRA_URBANO = 1;
    int DELIVERY = 2;
    int MESSAGING = 3;
    int CARGA = 4;
    int PET = 5;
    int FRIEND = 6;
}