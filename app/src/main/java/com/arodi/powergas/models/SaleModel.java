package com.arodi.powergas.models;

public class SaleModel {
    public String product_id;
    public String code;
    public String name;
    public String price;
    public String quantity;
    public String total;
    public String customer_id;
    public String discount;


    public SaleModel(String product_id, String code, String name, String price, String quantity, String total, String customer_id, String discount) {
        this.product_id = product_id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.total = total;
        this.customer_id = customer_id;
        this.discount = discount;
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

    public String getCustomer_id() {
        return customer_id;
    }

    public String getDiscount() {
        return discount;
    }
}
