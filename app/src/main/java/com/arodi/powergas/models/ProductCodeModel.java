package com.arodi.powergas.models;

public class ProductCodeModel {
        private String id;
        private String code;
        private String type;
        private String name;
        private String price;
        private String image;
        private String category;
        private String category_id;
        private String stock;
        private String sales;

        public ProductCodeModel(String id, String code, String type, String name, String price, String image,
                            String category, String category_id, String stock, String sales) {
            this.id = id;
            this.code = code;
            this.type = type;
            this.name = name;
            this.price = price;
            this.image = image;
            this.category = category;
            this.category_id = category_id;
            this.stock = stock;
            this.sales = sales;
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
