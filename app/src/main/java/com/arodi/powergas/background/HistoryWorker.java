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
import com.arodi.powergas.models.HistoryModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class HistoryWorker extends Worker {
    public static final String SHARED_PREFERENCE = "history_list";
    public static final String JSON_ARRAY = "response";

    public HistoryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);

        try {

            JSONArray jsonArray = new JSONArray(sharedPreferences.getString(JSON_ARRAY,""));

            new Database(getApplicationContext()).clear_history();

            System.out.println("gdhdjhhjcjd"+jsonArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                HistoryModel model = new HistoryModel(
                        jsonObject.getString("id"),
                        jsonObject.getString("date"),
                        jsonObject.getString("customer").replace("'",""),
                        jsonObject.getString("customer_id"),
                        jsonObject.getString("payment_status"),
                        jsonObject.getString("grand_total"),
                        jsonObject.getString("products"),
                        jsonObject.getString("shop_name").replace("'",""),
                        jsonObject.getString("lat"),
                        jsonObject.getString("lng"),
                        jsonObject.getString("image"),
                        jsonObject.getString("phone"),
                        jsonObject.getString("customer_group_name"),
                        jsonObject.getString("city"),
                        jsonObject.getString("shop_id"),
                        jsonObject.getString("payments"),
                        jsonObject.getString("updated_at"));

                new Database(getApplicationContext()).create_history(model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success();
    }
}
