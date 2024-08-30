package com.arodi.powergas.models;

public class SaleModelCode {
    private String saleId;
    private String productId;
    private String productName;
    private String productCode;
    private String productType;
    private String productCategory;
    private String productCategoryId;
    private String productPrice;
    private String productImage;
    private String productStock;
    private String productSales;
    private String discount;
    private String total;

    // Constructor
    public SaleModelCode(String saleId, String productId, String productName, String productCode, String productType,
                     String productCategory, String productCategoryId, String productPrice, String productImage,
                     String productStock, String productSales, String discount, String total) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.productType = productType;
        this.productCategory = productCategory;
        this.productCategoryId = productCategoryId;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.productStock = productStock;
        this.productSales = productSales;
        this.discount = discount;
        this.total = total;
    }


    // Getters and Setters
    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(String productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getProductStock() {
        return productStock;
    }

    public void setProductStock(String productStock) {
        this.productStock = productStock;
    }

    public String getProductSales() {
        return productSales;
    }

    public void setProductSales(String productSales) {
        this.productSales = productSales;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
