package com.example.femtaxi.models;

public class registroDriver2 {
    String numeroplaca; //txtnumeroplaca
    String numerocarro; //txtnumerocarro
    String modelocarro; //txtmodelocarro
    String tipocarro; //txttipocarro
    String userId;

    public registroDriver2() {
    }

    public registroDriver2(String numeroplaca, String numerocarro, String modelocarro, String tipocarro, String userId) {
        this.numeroplaca = numeroplaca;
        this.numerocarro = numerocarro;
        this.modelocarro = modelocarro;
        this.tipocarro = tipocarro;
        this.userId = userId;
    }

    public String getNumeroplaca() {
        return numeroplaca;
    }

    public void setNumeroplaca(String numeroplaca) {
        this.numeroplaca = numeroplaca;
    }

    public String getNumerocarro() {
        return numerocarro;
    }

    public void setNumerocarro(String numerocarro) {
        this.numerocarro = numerocarro;
    }

    public String getModelocarro() {
        return modelocarro;
    }

    public void setModelocarro(String modelocarro) {
        this.modelocarro = modelocarro;
    }



    public String getTipocarro() {
        return tipocarro;
    }

    public void setTipocarro(String tipocarro) {
        this.tipocarro = tipocarro;
    }



    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "registroDriver2{" +
                "numeroplaca='" + numeroplaca + '\'' +
                ", numerocarro='" + numerocarro + '\'' +
                ", modelocarro='" + modelocarro + '\'' +
                ", tipocarro='" + tipocarro + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}

