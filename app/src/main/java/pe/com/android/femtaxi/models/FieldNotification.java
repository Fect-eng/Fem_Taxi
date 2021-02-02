package pe.com.android.femtaxi.models;

import androidx.annotation.Keep;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;


@IgnoreExtraProperties
@Keep
public class FieldNotification<T> implements Serializable {
    private String to;
    private String title;
    private String body;
    private double price;
    private String priority;
    private String ttl;
    private T data;

    public FieldNotification() {
    }

    public FieldNotification(String to, String title, String body,
                             double price, String priority, String ttl,
                             T data) {
        this.to = to;
        this.title = title;
        this.body = body;
        this.price = price;
        this.priority = priority;
        this.ttl = ttl;
        this.data = data;
    }
}