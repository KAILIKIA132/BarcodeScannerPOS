package com.arodi.powergas.models;

public class StoreModel {
    public String id;
    public String product_name;
    public String product_quantity;
    public String product_id;
    public String product_price;

    public StoreModel(String id, String product_name, String product_quantity, String product_id, String product_price) {
        this.id = id;
        this.product_name = product_name;
        this.product_quantity = product_quantity;
        this.product_id = product_id;
        this.product_price = product_price;
    }

    public String getId() {
        return id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getProduct_quantity() {
        return product_quantity;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getProduct_price() {
        return product_price;
    }
}
