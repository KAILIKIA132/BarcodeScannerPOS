package com.arodi.powergas.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.StockAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityStockBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.ProductModel;
import com.arodi.powergas.models.ProductQuantity;
import com.arodi.powergas.models.StockModel;
import com.arodi.powergas.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockActivity extends AppCompatActivity {
    ActivityStockBinding binding;
    StockAdapter adapter;
    ArrayList<StockModel> stockModel = new ArrayList<>();
    HashMap<String, String> user;
    int day;
    private static final String TAG = "StockActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_stock);
        user = new SessionManager(StockActivity.this).getLoginDetails();

        binding.back.setOnClickListener(view -> onBackPressed());
        binding.RecyclerView.setHasFixedSize(true);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(StockActivity.this));
        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(() -> {
            if (NetworkState.getInstance(StockActivity.this).isConnected()) {
                getProducts();
            }
        });

        populateStock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProducts();
    }

    private void fetchProductQuantity() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "products/productsEndpoint.php?action=fetch_product_quantity&vehicle_id=" + user.get(SessionManager.KEY_VEHICLE_ID) + "&distributor_id=" + user.get(SessionManager.KEY_DISTRIBUTOR_ID))
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    binding.Refresh.setRefreshing(false);
                    Log.e(TAG, "fetchProductQuantity - onFailure: ", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    binding.Refresh.setRefreshing(false);
                    try {
                        if (response.body() != null) {
                            String responseBody = response.body().string();
                            Log.d(TAG, "fetchProductQuantity - onResponse: " + responseBody);
                            JSONArray jsonArray = new JSONArray(responseBody);

                            new Database(StockActivity.this.getBaseContext()).clear_product_quantity();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                ProductQuantity model = new ProductQuantity(
                                        jsonObject.getString("product_id"),
                                        jsonObject.getString("quantity"));

                                new Database(getBaseContext()).create_product_quantity(model);
                            }
                            populateStock();
                        } else {
                            Log.e(TAG, "fetchProductQuantity - onResponse: Response body is null");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "fetchProductQuantity - onResponse: ", e);
                    }
                });
            }
        });
    }

    private void getProducts() {
        binding.Refresh.setRefreshing(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        Log.d(TAG, "getProducts - Day of Week: " + dayOfWeek);

        if (dayOfWeek != null) {
            switch (dayOfWeek) {
                case "Monday":
                    day = 1;
                    break;
                case "Tuesday":
                    day = 2;
                    break;
                case "Wednesday":
                    day = 3;
                    break;
                case "Thursday":
                    day = 4;
                    break;
                case "Friday":
                    day = 5;
                    break;
                case "Saturday":
                    day = 6;
                    break;
                case "Sunday":
                    day = 7;
                    break;
            }
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "productsApi.php")
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    binding.Refresh.setRefreshing(false);
                    Log.e(TAG, "getProducts - onFailure: ", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        if (response.body() != null) {
                            String responseBody = response.body().string();
                            Log.d(TAG, "getProducts - onResponse: " + responseBody);

                            JSONObject jsonResponse = new JSONObject(responseBody);
                            JSONArray jsonArray = jsonResponse.getJSONArray("data");

                            new Database(StockActivity.this.getBaseContext()).clear_products();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                ProductModel model = new ProductModel(
                                        jsonObject.getString("id"),
                                        jsonObject.getString("code"),
                                        jsonObject.getString("name"),
                                        jsonObject.getString("price"),
                                        jsonObject.getString("category"),
                                        jsonObject.getString("category_id"),
                                        jsonObject.optString("stock", "0.0000"),  // Using optString with default value
                                        jsonObject.optString("type", ""),         // Using optString to handle possible null values
                                        jsonObject.optString("image", ""),        // Using optString to handle possible null values
                                        jsonObject.optString("sales", ""),         // Using optString to handle possible null values
                               "","","","","","","","",""

                                );

                                new Database(getBaseContext()).create_product(model);
                            }
                            fetchProductQuantity();
                        } else {
                            Log.e(TAG, "getProducts - onResponse: Response body is null");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getProducts - onResponse: ", e);
                    }
                });
            }
        });
    }

    private void initAdapter() {
        try {
            adapter = new StockAdapter(StockActivity.this, stockModel);
            binding.RecyclerView.setAdapter(adapter);

            int total = 0;
            for (StockModel model : stockModel) {
                total += Integer.parseInt(model.getStock());
            }

            String enableStock = user.get(SessionManager.KEY_ENABLE_STOCK);
            if (enableStock != null && enableStock.equals("1")) {
                binding.TotalItems.setText(total + " Items");
            } else {
                binding.TotalItems.setText("null Items");
            }

            if (stockModel.isEmpty()) {
                binding.RelNo.setVisibility(View.VISIBLE);
                binding.Head.setVisibility(View.GONE);
            } else {
                binding.RelNo.setVisibility(View.GONE);
                binding.Head.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "initAdapter: ", e);
        }
    }

    public void populateStock() {
        try {
            stockModel.clear();
            stockModel = (ArrayList<StockModel>) new Database(StockActivity.this).fetch_stock();
            initAdapter();
        } catch (Exception e) {
            Log.e(TAG, "populateStock: ", e);
        }
    }
}
