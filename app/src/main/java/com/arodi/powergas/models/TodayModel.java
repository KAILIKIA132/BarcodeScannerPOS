package com.arodi.powergas.models;

public class TodayModel {
    public String product_id;
    public String name;
    public String quantity;
    public String total;

    public TodayModel(String product_id, String name, String quantity, String total) {
        this.product_id = product_id;
        this.name = name;
        this.quantity = quantity;
        this.total = total;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getTotal() {
        return total;
    }
}
