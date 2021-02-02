package pe.com.android.femtaxi.models;

import java.io.Serializable;

public class ServiceNotification implements Serializable {
    private String title;
    private String body;
    private String idClient;
    private String addressOrigin;
    private String addressDestination;
    private String minutes;
    private String distance;

    public ServiceNotification() {
    }

    public ServiceNotification(String title, String body, String idClient, String addressOrigin,
                               String addressDestination, String minutes, String distance) {
        this.title = title;
        this.body = body;
        this.idClient = idClient;
        this.addressOrigin = addressOrigin;
        this.addressDestination = addressDestination;
        this.minutes = minutes;
        this.distance = distance;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getAddressOrigin() {
        return addressOrigin;
    }

    public void setAddressOrigin(String addressOrigin) {
        this.addressOrigin = addressOrigin;
    }

    public String getAddressDestination() {
        return addressDestination;
    }

    public void setAddressDestination(String addressDestination) {
        this.addressDestination = addressDestination;
    }

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "ServiceNotification{" +
                "title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", idClient='" + idClient + '\'' +
                ", addressOrigin='" + addressOrigin + '\'' +
                ", addressDestination='" + addressDestination + '\'' +
                ", minutes='" + minutes + '\'' +
                ", distance='" + distance + '\'' +
                '}';
    }
}