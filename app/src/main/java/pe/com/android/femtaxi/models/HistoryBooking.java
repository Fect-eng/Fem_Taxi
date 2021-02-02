package pe.com.android.femtaxi.models;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class HistoryBooking implements Serializable {
    private String idHistory;
    private String destination;
    private Double destinationLat;
    private Double destinationLong;
    private String idClient;
    private String idDriver;
    private String km;
    private String origin;
    private Double originLat;
    private Double originLong;
    private String status;
    private String time;
    private double calificationClient;
    private double calificationDrive;
    private long timesTamp;
    private double price;

    public HistoryBooking() {
    }

    public HistoryBooking(String idHistory, String destination, Double destinationLat,
                          Double destinationLong, String idClient, String idDriver, String km,
                          String origin, Double originLat, Double originLong, String status,
                          String time, double price) {
        this.idHistory = idHistory;
        this.destination = destination;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
        this.idClient = idClient;
        this.idDriver = idDriver;
        this.km = km;
        this.origin = origin;
        this.originLat = originLat;
        this.originLong = originLong;
        this.status = status;
        this.time = time;
        this.price = price;
    }

    public String getIdHistory() {
        return idHistory;
    }

    public void setIdHistory(String idHistory) {
        this.idHistory = idHistory;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(Double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public Double getDestinationLong() {
        return destinationLong;
    }

    public void setDestinationLong(Double destinationLong) {
        this.destinationLong = destinationLong;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getIdDriver() {
        return idDriver;
    }

    public void setIdDriver(String idDriver) {
        this.idDriver = idDriver;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(Double originLat) {
        this.originLat = originLat;
    }

    public Double getOriginLong() {
        return originLong;
    }

    public void setOriginLong(Double originLong) {
        this.originLong = originLong;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getCalificationClient() {
        return calificationClient;
    }

    public void setCalificationClient(double calificationClient) {
        this.calificationClient = calificationClient;
    }

    public double getCalificationDrive() {
        return calificationDrive;
    }

    public void setCalificationDrive(double calificationDrive) {
        this.calificationDrive = calificationDrive;
    }

    public long getTimesTamp() {
        return timesTamp;
    }

    public void setTimesTamp(long timesTamp) {
        this.timesTamp = timesTamp;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "HistoryBooking{" +
                "idHistory='" + idHistory + '\'' +
                ", destination='" + destination + '\'' +
                ", destinationLat=" + destinationLat +
                ", destinationLong=" + destinationLong +
                ", idClient='" + idClient + '\'' +
                ", idDriver='" + idDriver + '\'' +
                ", km='" + km + '\'' +
                ", origin='" + origin + '\'' +
                ", originLat=" + originLat +
                ", originLong=" + originLong +
                ", status='" + status + '\'' +
                ", time='" + time + '\'' +
                ", calificationClient=" + calificationClient +
                ", calificationDrive=" + calificationDrive +
                ", timesTamp=" + timesTamp +
                ", price=" + price +
                '}';
    }
}
