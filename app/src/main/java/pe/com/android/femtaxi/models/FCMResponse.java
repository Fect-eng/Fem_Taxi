package pe.com.android.femtaxi.models;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class FCMResponse implements Serializable {

    private String message_id;

    public FCMResponse() {
    }

    public FCMResponse(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    @Override
    public String toString() {
        return "FCMResponse{" +
                "message_id='" + message_id + '\'' +
                '}';
    }
}
