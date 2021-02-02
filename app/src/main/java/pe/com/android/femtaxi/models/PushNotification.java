package pe.com.android.femtaxi.models;

import java.io.Serializable;

public class PushNotification implements Serializable {
    private String to;
    private FieldNotification data;

    public PushNotification() {
    }

    public PushNotification(String to, FieldNotification data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public FieldNotification getData() {
        return data;
    }

    public void setData(FieldNotification data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PushNotification{" +
                "to='" + to + '\'' +
                ", data=" + data +
                '}';
    }
}
