package com.arodi.powergas.models;

public class ProductQuantity {
    public String product_id;
    public String quantity;

    public ProductQuantity(String product_id, String quantity) {
        this.product_id = product_id;
        this.quantity = quantity;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getQuantity() {
        return quantity;
    }
}
