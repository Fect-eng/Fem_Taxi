package com.example.femtaxi.models;

import java.util.Map;

public class FCMRequest {
    private String to;
    private String priority;
    Map<String, String> data;

    public FCMRequest(String to, String priority, Map<String, String> data) {
        this.to = to;
        this.priority = priority;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "FCMRequest{" +
                "to='" + to + '\'' +
                ", priority='" + priority + '\'' +
                ", data=" + data +
                '}';
    }
}
