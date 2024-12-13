package com.arodi.powergas.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductCodeModel implements Parcelable {
    private String id;
    private String code;
    private String type;
    private String name;
    private String price;        // Latest price
    private String initialPrice; // Initial price
    private String image;
    private String category;
    private String category_id;
    private String stock;
    private String sales;

    // Constructor
    public ProductCodeModel(String id, String code, String type, String name, String price, String initialPrice,
                            String image, String category, String category_id, String stock, String sales) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.name = name;
        this.price = price;
        this.initialPrice = initialPrice;  // Store the initial price
        this.image = image;
        this.category = category;
        this.category_id = category_id;
        this.stock = stock;
        this.sales = sales;
    }

    protected ProductCodeModel(Parcel in) {
        id = in.readString();
        code = in.readString();
        type = in.readString();
        name = in.readString();
        price = in.readString();
        initialPrice = in.readString();  // Read initial price from Parcel
        image = in.readString();
        category = in.readString();
        category_id = in.readString();
        stock = in.readString();
        sales = in.readString();
    }

    public static final Creator<ProductCodeModel> CREATOR = new Creator<ProductCodeModel>() {
        @Override
        public ProductCodeModel createFromParcel(Parcel in) {
            return new ProductCodeModel(in);
        }

        @Override
        public ProductCodeModel[] newArray(int size) {
            return new ProductCodeModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(code);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(initialPrice);  // Write initial price to Parcel
        dest.writeString(image);
        dest.writeString(category);
        dest.writeString(category_id);
        dest.writeString(stock);
        dest.writeString(sales);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getInitialPrice() {
        return initialPrice;  // Get initial price
    }

    public String getImage() {
        return image;
    }

    public String getCategory() {
        return category;
    }

    public String getCategory_id() {
        return category_id;
    }

    public String getStock() {
        return stock;
    }

    public String getSales() {
        return sales;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setInitialPrice(String initialPrice) {
        this.initialPrice = initialPrice;  // Set initial price
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public void setSales(String sales) {
        this.sales = sales;
    }
}
