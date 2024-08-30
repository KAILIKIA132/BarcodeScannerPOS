package com.arodi.powergas.models;

public class CityModel {
    public String id;
    public String city;
    public String county_id;
    public String county_name;
    
    public CityModel(String id, String city, String county_id, String county_name) {
        this.id = id;
        this.city = city;
        this.county_id = county_id;
        this.county_name = county_name;
    }
    
    public String getId() {
        return id;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getCounty_id() {
        return county_id;
    }

    public String getCounty_name() {
        return county_name;
    }
}
