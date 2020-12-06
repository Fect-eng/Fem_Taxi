package com.example.femtaxi.models;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class ClientBooking implements Serializable {
    private String idHistory;
    private String destination;
    private double destinationLat;
    private double destinationLong;
    private String idClient;
    private String idDriver;
    private String km;
    private String origin;
    private double originLat;
    private double originLong;
    private String status;
    private String time;
    private double price;

    public ClientBooking() {
    }

    public ClientBooking(String idHistory, String destination, double destinationLat,
                         double destinationLong, String idClient, String idDriver, String km,
                         String origin, double originLat, double originLong, String status,
                         String time,double price) {
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

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLong() {
        return destinationLong;
    }

    public void setDestinationLong(double destinationLong) {
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

    public double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(double originLat) {
        this.originLat = originLat;
    }

    public double getOriginLong() {
        return originLong;
    }

    public void setOriginLong(double originLong) {
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ClientBooking{" +
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
                ", price=" + price +
                '}';
    }
}
