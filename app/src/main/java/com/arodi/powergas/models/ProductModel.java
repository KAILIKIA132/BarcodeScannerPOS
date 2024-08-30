package com.arodi.powergas.models;

public class ProductModel {
    public String product_id;
    public String product_code;
    public String product_name;
    public String price;
    public String quantity;
    public String plate_no;
    public String discount_enabled;
    public String target;
    public String portion1;
    public String portion1qty;
    public String portion2;
    public String portion2qty;
    public String portion3;
    public String portion3qty;
    public String portion4;
    public String portion4qty;
    public String portion5;
    public String portion5qty;
    public String isKitchen;

    public ProductModel(String product_id, String product_code, String product_name, String price, String quantity, String plate_no, String discount_enabled, String target, String portion1, String portion1qty, String portion2, String portion2qty, String portion3, String portion3qty, String portion4, String portion4qty, String portion5, String portion5qty, String isKitchen) {
        this.product_id = product_id;
        this.product_code = product_code;
        this.product_name = product_name;
        this.price = price;
        this.quantity = quantity;
        this.plate_no = plate_no;
        this.discount_enabled = discount_enabled;
        this.target = target;
        this.portion1 = portion1;
        this.portion1qty = portion1qty;
        this.portion2 = portion2;
        this.portion2qty = portion2qty;
        this.portion3 = portion3;
        this.portion3qty = portion3qty;
        this.portion4 = portion4;
        this.portion4qty = portion4qty;
        this.portion5 = portion5;
        this.portion5qty = portion5qty;
        this.isKitchen = isKitchen;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getProduct_code() {
        return product_code;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPlate_no() {
        return plate_no;
    }

    public String getDiscount_enabled() {
        return discount_enabled;
    }

    public String getTarget() {
        return target;
    }

    public String getPortion1() {
        return portion1;
    }

    public String getPortion1qty() {
        return portion1qty;
    }

    public String getPortion2() {
        return portion2;
    }

    public String getPortion2qty() {
        return portion2qty;
    }

    public String getPortion3() {
        return portion3;
    }

    public String getPortion3qty() {
        return portion3qty;
    }

    public String getPortion4() {
        return portion4;
    }

    public String getPortion4qty() {
        return portion4qty;
    }

    public String getPortion5() {
        return portion5;
    }

    public String getPortion5qty() {
        return portion5qty;
    }

    public String getIsKitchen() {
        return isKitchen;
    }
}
