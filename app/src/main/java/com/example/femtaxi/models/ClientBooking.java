package com.example.femtaxi.models;

public class ClientBooking {
    private String idHistory;
    private String destination;
    private String destinationLat;
    private String destinationLong;
    private String idClient;
    private String idDriver;
    private String km;
    private String origin;
    private String originLat;
    private String originLong;
    private String status;
    private String time;

    public ClientBooking(String idHistory, String destination, String destinationLat,
                         String destinationLong, String idClient, String idDriver, String km,
                         String origin, String originLat, String originLong, String status, String time) {
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

    public String getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(String destinationLat) {
        this.destinationLat = destinationLat;
    }

    public String getDestinationLong() {
        return destinationLong;
    }

    public void setDestinationLong(String destinationLong) {
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

    public String getOriginLat() {
        return originLat;
    }

    public void setOriginLat(String originLat) {
        this.originLat = originLat;
    }

    public String getOriginLong() {
        return originLong;
    }

    public void setOriginLong(String originLong) {
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

    @Override
    public String toString() {
        return "ClientBooking{" +
                "idHistory='" + idHistory + '\'' +
                ", destination='" + destination + '\'' +
                ", destinationLat='" + destinationLat + '\'' +
                ", destinationLong='" + destinationLong + '\'' +
                ", idClient='" + idClient + '\'' +
                ", idDriver='" + idDriver + '\'' +
                ", km='" + km + '\'' +
                ", origin='" + origin + '\'' +
                ", originLat='" + originLat + '\'' +
                ", originLong='" + originLong + '\'' +
                ", status='" + status + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
