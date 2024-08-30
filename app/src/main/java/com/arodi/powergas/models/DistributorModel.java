package com.arodi.powergas.models;

public class DistributorModel {
    public String id;
    public String name;

    public DistributorModel(String id, String name) {
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
