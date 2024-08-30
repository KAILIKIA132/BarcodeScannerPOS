package com.arodi.powergas.models;

import android.os.Parcel;
import android.os.Parcelable;

public class PeopleModel implements Parcelable {
    public String id;
    public String name;
    public String email;
    public String phone;
    public String plate_no;
    public String distributor_id;
    public String vehicle_id;
    public String route_id;
    public String route_name;


    public PeopleModel(String id, String name, String email, String phone, String plate_no, String distributor_id, String vehicle_id, String route_id, String route_name) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.plate_no = plate_no;
        this.distributor_id = distributor_id;
        this.vehicle_id = vehicle_id;
        this.route_id = route_id;
        this.route_name = route_name;
    }

    protected PeopleModel(Parcel in) {
        id = in.readString();
        name = in.readString();
        email = in.readString();
        phone = in.readString();
        plate_no = in.readString();
        distributor_id = in.readString();
        vehicle_id = in.readString();
        route_id = in.readString();
        route_name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(plate_no);
        dest.writeString(distributor_id);
        dest.writeString(vehicle_id);
        dest.writeString(route_id);
        dest.writeString(route_name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PeopleModel> CREATOR = new Creator<PeopleModel>() {
        @Override
        public PeopleModel createFromParcel(Parcel in) {
            return new PeopleModel(in);
        }

        @Override
        public PeopleModel[] newArray(int size) {
            return new PeopleModel[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPlate_no() {
        return plate_no;
    }

    public String getDistributor_id() {
        return distributor_id;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public String getRoute_id() {
        return route_id;
    }

    public String getRoute_name() {
        return route_name;
    }
}
