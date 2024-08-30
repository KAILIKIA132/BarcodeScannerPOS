package com.arodi.powergas.models;

public class ProductHistory {
    public String product_id;
    public String code;
    public String name;
    public String price;
    public String quantity;
    public String total;

    public ProductHistory(String product_id, String code, String name, String price, String quantity, String total) {
        this.product_id = product_id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.total = total;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getTotal() {
        return total;
    }
}
