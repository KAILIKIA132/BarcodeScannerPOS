package com.arodi.powergas.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.arodi.powergas.models.HistoryModel;

import java.io.Serializable;

@Entity
public class InvoiceEntity implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "sale_id")
    private String sale_id;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "customer")
    private String customer;

    @ColumnInfo(name = "customer_id")
    private String customer_id;


    @ColumnInfo(name = "payment_status")
    private String payment_status;

    @ColumnInfo(name = "grand_total")
    private String grand_total;

    @ColumnInfo(name = "products")
    private String products;

    @ColumnInfo(name = "shop_name")
    private String shop_name;

    @ColumnInfo(name = "lat")
    private String lat;

    @ColumnInfo(name = "lng")
    private String lng;

    @ColumnInfo(name = "image")
    private String image;

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "customer_group_name")
    private String customer_group_name;


    @ColumnInfo(name = "city")
    private String city;

    @ColumnInfo(name = "shop_id")
    private String shop_id;

    @ColumnInfo(name = "payments")
    private String payments;

    @ColumnInfo(name = "updated_at")
    private String updated_at;

    public InvoiceEntity(String sale_id, String date, String customer, String customer_id, String payment_status, String grand_total, String products, String shop_name, String lat, String lng, String image, String phone, String customer_group_name, String city, String shop_id, String payments, String updated_at) {
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

    protected InvoiceEntity(Parcel in) {
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

    public static final Parcelable.Creator<InvoiceEntity> CREATOR = new Parcelable.Creator<InvoiceEntity>() {
        @Override
        public InvoiceEntity createFromParcel(Parcel in) {
            return new InvoiceEntity(in);
        }

        @Override
        public InvoiceEntity[] newArray(int size) {
            return new InvoiceEntity[size];
        }
    };


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSale_id() {
        return sale_id;
    }

    public void setSale_id(String sale_id) {
        this.sale_id = sale_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getGrand_total() {
        return grand_total;
    }

    public void setGrand_total(String grand_total) {
        this.grand_total = grand_total;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCustomer_group_name() {
        return customer_group_name;
    }

    public void setCustomer_group_name(String customer_group_name) {
        this.customer_group_name = customer_group_name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getShop_id() {
        return shop_id;
    }

    public void setShop_id(String shop_id) {
        this.shop_id = shop_id;
    }

    public String getPayments() {
        return payments;
    }

    public void setPayments(String payments) {
        this.payments = payments;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
