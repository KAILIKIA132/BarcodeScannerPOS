package com.arodi.powergas.models;

public class PaymentModel {
    public String id;
    public String name;

    public PaymentModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
