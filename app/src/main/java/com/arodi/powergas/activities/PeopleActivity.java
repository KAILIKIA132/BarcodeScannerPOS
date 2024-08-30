package com.arodi.powergas.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.PeopleAdapter;
import com.arodi.powergas.adapters.TodayAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityPeopleBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.PeopleModel;
import com.arodi.powergas.models.TodayModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PeopleActivity extends AppCompatActivity {
    PeopleAdapter adapter;
    ArrayList<PeopleModel> peopleModel = new ArrayList<>();
    ActivityPeopleBinding binding;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this,R.layout.activity_people);

        binding.RecyclerView.setHasFixedSize(true);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(PeopleActivity.this));

        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(this::getPeople);

        binding.SearchPeople.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    searchPeople(s.toString());
                } else {
                    populatePeople();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(binding.SearchPeople.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        populatePeople();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkState.getInstance(PeopleActivity.this).isConnected()) {
            getPeople();
        }
    }

    public void searchPeople(String s) {
        peopleModel.clear();
        peopleModel = (ArrayList<PeopleModel>) new Database(getBaseContext()).search_people(s);
        initAdapter();
    }

    private void getPeople() {
        binding.Refresh.setRefreshing(true);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "getVehicleData")
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
                runOnUiThread(() -> {
                    binding.Refresh.setRefreshing(false);
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        System.out.println("jsonObjectiiddddiii" + jsonArray);

                        new Database(getBaseContext()).clear_people();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            PeopleModel model = new PeopleModel(
                                    jsonObject.getString("id"),
                                    jsonObject.getString("name"),
                                    jsonObject.getString("email"),
                                    jsonObject.getString("phone"),
                                    jsonObject.getString("plate_no"),
                                    jsonObject.getString("distributor_id"),
                                    jsonObject.getString("vehicle_id"),
                                    jsonObject.getString("route_id"),
                                    jsonObject.getString("route_name"));

                            new Database(getBaseContext()).create_people(model);
                        }
                        populatePeople();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    private void initAdapter() {
        adapter = new PeopleAdapter(PeopleActivity.this, peopleModel);
        adapter.notifyDataSetChanged();
        binding.RecyclerView.setAdapter(adapter);

        if (peopleModel.size() == 0) {
            binding.RelNo.setVisibility(View.VISIBLE);
        } else {
            binding.RelNo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, "Press back again to logout", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3500);
    }

    public void populatePeople() {
        peopleModel.clear();
        peopleModel = (ArrayList<PeopleModel>) new Database(PeopleActivity.this).fetch_people();
        initAdapter();
    }
}