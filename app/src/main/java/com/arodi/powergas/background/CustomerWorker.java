package com.arodi.powergas.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.models.CustomerModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class CustomerWorker extends Worker {
    public static final String SHARED_PREFERENCE = "customers_list";
    public static final String JSON_ARRAY = "response";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    public CustomerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        try {
            JSONArray jsonArray = new JSONArray(sharedPreferences.getString(JSON_ARRAY,""));

            Location current = new Location(LocationManager.GPS_PROVIDER);
            current.setLatitude(Double.parseDouble(sharedPreferences.getString(LATITUDE,"0.0")));
            current.setLongitude(Double.parseDouble(sharedPreferences.getString(LONGITUDE, "0.0")));

            new Database(getApplicationContext()).clear_customers();

            System.out.println("gdhdjhhjcjd" + jsonArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Location customer = new Location(LocationManager.GPS_PROVIDER);
//                customer.setLatitude(Double.parseDouble(jsonObject.getString("lat")));
//                customer.setLongitude(Double.parseDouble(jsonObject.getString("lng")));
                CustomerModel model = new CustomerModel(
                        jsonObject.optString("id", "1"),
                        jsonObject.optString("name", "kj").replace("'", ""),
                        jsonObject.optString("customer_group_name", "default_group_name"),
                        jsonObject.optString("city", "default_county_name"),
                        jsonObject.optString("phone", "07889999"),
                        jsonObject.optString("email", "").replace("'", ""),
                        jsonObject.optString("logo", "default_logo.jpg"),
                        "0.99","78.00",
                        jsonObject.optString("customer_group_id", "9"),
                        "Amazing","Shariani","89","90",
                        new DecimalFormat("#.#").format(current.distanceTo(customer) / 1000)
                );



                new Database(getApplicationContext()).create_customer(model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success();
    }
}
