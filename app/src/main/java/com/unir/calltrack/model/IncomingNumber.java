package com.unir.calltrack.model;

public class IncomingNumber {
    private int id;
    private String number;
    private String name;
    private int qtd;

    public IncomingNumber (int id, String number, String name, int qtd) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.qtd = qtd;
    }

    public int getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQtd() {
        return qtd;
    }
}
