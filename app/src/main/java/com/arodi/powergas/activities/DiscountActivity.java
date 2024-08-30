package com.arodi.powergas.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.DiscountAdapter;
import com.arodi.powergas.adapters.HistoryAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.background.DiscountWorker;
import com.arodi.powergas.background.HistoryWorker;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityDiscountBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.HistoryModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.material.internal.NavigationMenu;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DiscountActivity extends AppCompatActivity {
    public DiscountAdapter discountAdapter;
    public ArrayList<HistoryModel> arrayList = new ArrayList<>();
    public boolean isLoading = false;
    HashMap<String, String> user;
    ActivityDiscountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_discount);
        binding.back.setOnClickListener(view -> onBackPressed());

        user = new SessionManager(DiscountActivity.this).getLoginDetails();
        populateDiscount();

        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(
                this::getDiscount
        );


        binding.PDFDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_customer:
                        startActivity(new Intent(DiscountActivity.this, CustomerActivity.class));
                        break;
                    case R.id.action_route:
                        startActivity(new Intent(DiscountActivity.this, RouteActivity.class));
                        break;
                    case R.id.action_shop:
                        startActivity(new Intent(DiscountActivity.this, NewCustomerActivity.class));
                        break;
                }
                return true;
            }
        });

        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(DiscountActivity.this));

        binding.SearchCustomers.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    searchDiscount(s.toString());
                } else {
                    populateDiscount();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(binding.SearchCustomers.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkState.getInstance(DiscountActivity.this).isConnected()) {
            getDiscount();
        }
    }

    private void getDiscount() {
        binding.Refresh.setRefreshing(true);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "sales?action=fetch_discount&salesman_id="+user.get(SessionManager.KEY_SALESMAN_ID)+"&vehicle_id=" + user.get(SessionManager.KEY_VEHICLE_ID))
                .method("GET", null)
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
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences(DiscountWorker.SHARED_PREFERENCE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(DiscountWorker.JSON_ARRAY, response.body().string());
                    editor.apply();

                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DiscountWorker.class).build();
                    WorkManager.getInstance().enqueue(workRequest);

                    runOnUiThread(() -> {
                        if (DiscountActivity.this != null) {
                            if (workRequest != null) {
                                WorkManager.getInstance().getWorkInfoByIdLiveData(workRequest.getId()).observe(DiscountActivity.this, workInfo -> {
                                    if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                        populateDiscount();
                                        binding.Refresh.setRefreshing(false);
                                    }
                                });
                            }
                        }

                    });

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    public void searchDiscount(String s) {
        arrayList.clear();
        arrayList = (ArrayList<HistoryModel>) new Database(getBaseContext()).search_discount(s);
        initAdapter();
    }

    private void initAdapter() {
        discountAdapter = new DiscountAdapter(DiscountActivity.this, arrayList);
        discountAdapter.notifyDataSetChanged();
        binding.recyclerview.setAdapter(discountAdapter);

        if (arrayList.size() == 0) {
            binding.RelNo.setVisibility(View.VISIBLE);
        } else {
            binding.RelNo.setVisibility(View.GONE);
        }
    }

    private void initScrollListener() {
        binding.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == arrayList.size() - 1) {
                        //bottom of list!
                        if (dy > 0) {
                            loadMore();
                            isLoading = true;
                        }
                    }
                }
            }
        });


    }

    private void loadMore() {
        if (arrayList.size() != 0) {
            arrayList.add(null);

            binding.recyclerview.post(() ->  discountAdapter.notifyItemInserted(arrayList.size() - 1));

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (arrayList.size() != 0) {
                    arrayList.remove(arrayList.size() - 1);
                    int scrollPosition = arrayList.size();
                    discountAdapter.notifyItemRemoved(scrollPosition);
                    System.out.println("scrollPosition" + scrollPosition);
                    try {
                        JSONArray jsonArray = new JSONArray(new Gson().toJson(new Database(getBaseContext()).fetch_discount(scrollPosition)));

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            HistoryModel model = new HistoryModel(
                                    jsonObject.getString("sale_id"),
                                    jsonObject.getString("date"),
                                    jsonObject.getString("customer"),
                                    jsonObject.getString("customer_id"),
                                    jsonObject.getString("payment_status"),
                                    jsonObject.getString("grand_total"),
                                    jsonObject.getString("products"),
                                    jsonObject.getString("shop_name"),
                                    jsonObject.getString("lat"),
                                    jsonObject.getString("lng"),
                                    jsonObject.getString("image"),
                                    jsonObject.getString("phone"),
                                    jsonObject.getString("customer_group_name"),
                                    jsonObject.getString("city"),
                                    jsonObject.getString("shop_id"),
                                    jsonObject.getString("payments"),
                                    jsonObject.getString("updated_at"));
                            arrayList.add(model);

                            discountAdapter.notifyItemInserted(i);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    discountAdapter.notifyDataSetChanged();
                    isLoading = false;
                }
            }, 2000);

        }
    }


    public void populateDiscount() {
        arrayList.clear();
        arrayList = (ArrayList<HistoryModel>) new Database(DiscountActivity.this.getBaseContext()).fetch_discount(0);
        initAdapter();
        initScrollListener();
    }
}