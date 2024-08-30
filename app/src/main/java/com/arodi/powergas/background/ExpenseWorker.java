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
import com.arodi.powergas.models.ExpenditureModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class ExpenseWorker extends Worker {
    public static final String SHARED_PREFERENCE = "expense_list";
    public static final String JSON_ARRAY = "response";

    public ExpenseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);

        try {
            JSONArray jsonArray = new JSONArray(sharedPreferences.getString(JSON_ARRAY,""));

            new Database(getApplicationContext()).clear_expense();

            System.out.println("fhfgfhhdhff"+jsonArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ExpenditureModel model = new ExpenditureModel(
                        jsonObject.getString("id"),
                        jsonObject.getString("date"),
                        jsonObject.getString("company_id"),
                        jsonObject.getString("reference").replace("'",""),
                        jsonObject.getString("amount"),
                        jsonObject.getString("approved"),
                        jsonObject.getString("note").replace("'",""),
                        jsonObject.getString("status"));

                new Database(getApplicationContext()).create_expense(model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success();
    }
}
