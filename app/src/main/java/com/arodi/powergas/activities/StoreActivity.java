package com.arodi.powergas.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.StoreAdapter;
import com.arodi.powergas.adapters.TodayAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityStoreBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.CustomerModel;
import com.arodi.powergas.models.PeopleModel;
import com.arodi.powergas.models.StoreModel;
import com.arodi.powergas.models.TodayModel;
import com.arodi.powergas.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StoreActivity extends AppCompatActivity {

    ActivityStoreBinding binding;
    StoreAdapter adapter;
    ArrayList<StoreModel> storeModel = new ArrayList<>();
    PeopleModel peopleModel;
    HashMap<String, String> user = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_store);
        user = new SessionManager(StoreActivity.this).getLoginDetails();

        peopleModel = getIntent().getParcelableExtra("PEOPLE_INFORMATION");
        binding.back.setOnClickListener(view -> onBackPressed());
        binding.RecyclerView.setHasFixedSize(true);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(StoreActivity.this));

        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(() -> binding.Refresh.setRefreshing(false));
        new Database(getBaseContext()).clear_store();

        populateStore();

        if (NetworkState.getInstance(StoreActivity.this).isConnected()) {
            getStore();
        }

        binding.Submit.setOnClickListener(v -> {
            JSONArray jsonArray = adapter.getStock();
            if (jsonArray.length() == adapter.getItemCount()){
                pushStore(jsonArray);
                System.out.println("vkmvmfmdkkd"+jsonArray);

            }else {
                Toast.makeText(StoreActivity.this, "Please input all stock quantities", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getStore() {
        binding.Refresh.setRefreshing(true);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("vehicle_id", peopleModel.getVehicle_id())
                .addFormDataPart("distributor_id", peopleModel.getDistributor_id())
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "getStock")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("errorrr" + e.toString());
                    binding.Refresh.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    binding.Refresh.setRefreshing(false);
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        System.out.println("kkkkkkkkljsonObjectiiddddiii" + jsonArray);

                        new Database(getBaseContext()).clear_store();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            StoreModel model = new StoreModel(
                                    jsonObject.getString("id"),
                                    jsonObject.getString("product_name"),
                                    jsonObject.getString("product_quantity"),
                                    jsonObject.getString("product_id"),
                                    jsonObject.getString("product_price"));

                            new Database(getBaseContext()).create_store(model);
                        }
                        populateStore();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    private void pushStore(JSONArray jsonArray) {
        SweetAlertDialog dialog = new SweetAlertDialog(StoreActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitle("Please Wait...");
        dialog.setCancelable(false);
        dialog.show();


        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("vehicle_id", peopleModel.getVehicle_id())
                .addFormDataPart("salesman_id", peopleModel.getId())
                .addFormDataPart("distributor_id", peopleModel.getDistributor_id())
                .addFormDataPart("stock_taker_id", user.get(SessionManager.KEY_USER_ID))
                .addFormDataPart("expected_stock", jsonArray.toString())
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "postStock")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("errorrr" + e.toString());
                    Toast.makeText(StoreActivity.this, "No connection to host", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        System.out.println("cjcjcncjvf"+jsonObject);

                        if (jsonObject.getString("success").equals("1")) {
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            dialog.setTitle(jsonObject.getString("message"));
                            dialog.setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismiss();
                                onBackPressed();
                            });
                        }else {
                            dialog.dismiss();
                            Toast.makeText(StoreActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        dialog.dismiss();
                        Toast.makeText(StoreActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    private void initAdapter() {
        adapter = new StoreAdapter(StoreActivity.this, storeModel);
        adapter.notifyDataSetChanged();
        binding.RecyclerView.setAdapter(adapter);

        if (storeModel.size() == 0) {
            binding.RelNo.setVisibility(View.VISIBLE);
            binding.Head.setVisibility(View.GONE);
        } else {
            binding.RelNo.setVisibility(View.GONE);
            binding.Head.setVisibility(View.VISIBLE);
        }
    }

    public void populateStore() {
        storeModel.clear();
        storeModel = (ArrayList<StoreModel>) new Database(StoreActivity.this).fetch_store();
        initAdapter();
    }
}