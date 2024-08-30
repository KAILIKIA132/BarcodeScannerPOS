package com.arodi.powergas.models;

import android.os.Parcel;
import android.os.Parcelable;

public class HistoryModel implements Parcelable {

    public String sale_id;
    public String date;
    public String customer;
    public String customer_id;
    public String payment_status;
    public String grand_total;
    public String products;
    public String shop_name;
    public String lat;
    public String lng;
    public String image;
    public String phone;
    public String customer_group_name;
    public String city;
    public String shop_id;
    public String payments;
    public String updated_at;


    public HistoryModel(String sale_id, String date, String customer, String customer_id, String payment_status, String grand_total, String products, String shop_name, String lat, String lng, String image, String phone, String customer_group_name, String city, String shop_id, String payments, String updated_at) {
        this.sale_id = sale_id;
        this.date = date;
        this.customer = customer;
        this.customer_id = customer_id;
        this.payment_status = payment_status;
        this.grand_total = grand_total;
        this.products = products;
        this.shop_name = shop_name;
        this.lat = lat;
        this.lng = lng;
        this.image = image;
        this.phone = phone;
        this.customer_group_name = customer_group_name;
        this.city = city;
        this.shop_id = shop_id;
        this.payments = payments;
        this.updated_at = updated_at;
    }

    protected HistoryModel(Parcel in) {
        sale_id = in.readString();
        date = in.readString();
        customer = in.readString();
        customer_id = in.readString();
        payment_status = in.readString();
        grand_total = in.readString();
        products = in.readString();
        shop_name = in.readString();
        lat = in.readString();
        lng = in.readString();
        image = in.readString();
        phone = in.readString();
        customer_group_name = in.readString();
        city = in.readString();
        shop_id = in.readString();
        payments = in.readString();
        updated_at = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sale_id);
        dest.writeString(date);
        dest.writeString(customer);
        dest.writeString(customer_id);
        dest.writeString(payment_status);
        dest.writeString(grand_total);
        dest.writeString(products);
        dest.writeString(shop_name);
        dest.writeString(lat);
        dest.writeString(lng);
        dest.writeString(image);
        dest.writeString(phone);
        dest.writeString(customer_group_name);
        dest.writeString(city);
        dest.writeString(shop_id);
        dest.writeString(payments);
        dest.writeString(updated_at);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HistoryModel> CREATOR = new Creator<HistoryModel>() {
        @Override
        public HistoryModel createFromParcel(Parcel in) {
            return new HistoryModel(in);
        }

        @Override
        public HistoryModel[] newArray(int size) {
            return new HistoryModel[size];
        }
    };

    public String getSale_id() {
        return sale_id;
    }

    public String getDate() {
        return date;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public String getGrand_total() {
        return grand_total;
    }

    public String getProducts() {
        return products;
    }

    public String getShop_name() {
        return shop_name;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getImage() {
        return image;
    }

    public String getPhone() {
        return phone;
    }

    public String getCustomer_group_name() {
        return customer_group_name;
    }

    public String getCity() {
        return city;
    }

    public String getShop_id() {
        return shop_id;
    }

    public String getPayments() {
        return payments;
    }

    public String getUpdated_at() {
        return updated_at;
    }
}
