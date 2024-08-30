package com.arodi.powergas.models;

public class ProductModelTwo {
    private String product_id;
    private String product_code;
    private String product_name;
    private String price;
    private String quantity;
    private String image;
    private String category;
    private String category_id;
    private String stock;
    private String sales;
    private String type;

    public ProductModelTwo(String product_id, String product_code, String product_name, String price, String quantity, String image, String category, String category_id, String stock, String sales, String type) {
        this.product_id = product_id;
        this.product_code = product_code;
        this.product_name = product_name;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
        this.category = category;
        this.category_id = category_id;
        this.stock = stock;
        this.sales = sales;
        this.type = type;
    }

    // Getters
    public String getProduct_id() { return product_id; }
    public String getProduct_code() { return product_code; }
    public String getProduct_name() { return product_name; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public String getImage() { return image; }
    public String getCategory() { return category; }
    public String getCategory_id() { return category_id; }
    public String getStock() { return stock; }
    public String getSales() { return sales; }
    public String getType() { return type; }
}
