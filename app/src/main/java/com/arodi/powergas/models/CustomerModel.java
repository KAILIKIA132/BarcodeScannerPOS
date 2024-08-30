package com.arodi.powergas.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomerModel{
    public  String customer_id;
    public  String name;
    public  String customer_group_name;
    public  String county_name;
    public  String phone;
    public  String email;
    public  String logo;
    public  String lat;
    public  String lng;
    public  String customer_group_id;
    public  String shop_name;
    public String town_name;
    public String town_id;
    public String shop_id;
    public String distance;

    public CustomerModel(String customer_id, String name, String customer_group_name, String county_name, String phone, String email, String logo, String lat, String lng, String customer_group_id, String shop_name, String town_name, String town_id, String shop_id, String distance) {
        this.customer_id = customer_id;
        this.name = name;
        this.customer_group_name = customer_group_name;
        this.county_name = county_name;
        this.phone = phone;
        this.email = email;
        this.logo = logo;
        this.lat = lat;
        this.lng = lng;
        this.customer_group_id = customer_group_id;
        this.shop_name = shop_name;
        this.town_name = town_name;
        this.town_id = town_id;
        this.shop_id = shop_id;
        this.distance = distance;
    }


    public String getCustomer_id() {
        return customer_id;
    }

    public String getName() {
        return name;
    }

    public String getCustomer_group_name() {
        return customer_group_name;
    }

    public String getCounty_name() {
        return county_name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getLogo() {
        return logo;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getCustomer_group_id() {
        return customer_group_id;
    }

    public String getShop_name() {
        return shop_name;
    }

    public String getTown_name() {
        return town_name;
    }

    public String getTown_id() {
        return town_id;
    }

    public String getShop_id() {
        return shop_id;
    }

    public String getDistance() {
        return distance;
    }

}
