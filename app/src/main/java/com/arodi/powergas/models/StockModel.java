package com.arodi.powergas.models;

public class StockModel {
    public String id;
    public String name;
    public String stock;
    public String target;
    public String price;
    public String code;

    public StockModel(String id, String name, String stock, String target, String price, String code) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.target = target;
        this.price = price;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStock() {
        return stock;
    }

    public String getTarget() {
        return target;
    }

    public String getPrice() {
        return price;
    }

    public String getCode() {
        return code;
    }
}
