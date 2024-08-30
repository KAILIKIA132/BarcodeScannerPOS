package com.arodi.powergas.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.ChequeAdapter;
import com.arodi.powergas.adapters.DiscountAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.database.DatabaseClient;
import com.arodi.powergas.databinding.ActivityChequeBinding;
import com.arodi.powergas.entities.ChequeEntity;
import com.arodi.powergas.entities.InvoiceEntity;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.HistoryModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.material.internal.NavigationMenu;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChequeActivity extends AppCompatActivity {
    public ChequeAdapter chequeAdapter;
    public List<ChequeEntity> arrayList = new ArrayList<>();
    HashMap<String, String> user;
    ActivityChequeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cheque);
        binding.back.setOnClickListener(view -> onBackPressed());

        user = new SessionManager(ChequeActivity.this).getLoginDetails();
        populateCheque();

        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(
                this::getCheque
        );


        binding.PDFDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onPrepareMenu(@SuppressLint("RestrictedApi") NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_customer:
                        startActivity(new Intent(ChequeActivity.this, CustomerActivity.class));
                        break;
                    case R.id.action_route:
                        startActivity(new Intent(ChequeActivity.this, RouteActivity.class));
                        break;
                    case R.id.action_shop:
                        startActivity(new Intent(ChequeActivity.this, NewCustomerActivity.class));
                        break;
                }
                return true;
            }
        });

        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(ChequeActivity.this));

        binding.SearchCustomers.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    searchCheque(s.toString());
                } else {
                    populateCheque();
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
        if (NetworkState.getInstance(ChequeActivity.this).isConnected()) {
            getCheque();
        }
    }

    private void getCheque() {
        binding.Refresh.setRefreshing(true);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "sales?action=fetch_cheque&salesman_id="+user.get(SessionManager.KEY_SALESMAN_ID)+"&vehicle_id=" + user.get(SessionManager.KEY_VEHICLE_ID))
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
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    DatabaseClient.getInstance(getApplicationContext()).getDatabase().chequeDAO().deleteAll();

                    System.out.println("gdhdjhhjcjd"+jsonArray);
                    System.out.println("arrayLenth"+jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        ChequeEntity entity = new ChequeEntity(
                                jsonObject.getString("id"),
                                jsonObject.getString("date"),
                                jsonObject.getString("customer").replace("'", ""),
                                jsonObject.getString("customer_id"),
                                jsonObject.getString("status"),
                                jsonObject.getString("grand_total"),
                                jsonObject.getString("products"),
                                jsonObject.getString("shop_name").replace("'", ""),
                                jsonObject.getString("lat"),
                                jsonObject.getString("lng"),
                                jsonObject.getString("image"),
                                jsonObject.getString(SessionManager.KEY_PHONE),
                                jsonObject.getString("customer_group_name"),
                                jsonObject.getString("city"),
                                jsonObject.getString("shop_id"),
                                jsonObject.getString("payments"),
                                jsonObject.getString("updated_at")
                        );


                        //adding to database
                        DatabaseClient.getInstance(getApplicationContext()).getDatabase().chequeDAO().insert(entity);
                    }

                    runOnUiThread(() -> {
                        binding.Refresh.setRefreshing(false);
                        populateCheque();
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    public void searchCheque(String s) {
        arrayList.clear();
        class GetCheques extends AsyncTask<Void, Void, List<ChequeEntity>> {

            @Override
            protected List<ChequeEntity> doInBackground(Void... voids) {
                return DatabaseClient
                        .getInstance(getApplicationContext())
                        .getDatabase()
                        .chequeDAO()
                        .searchInvoice(s);
            }

            @Override
            protected void onPostExecute(List<ChequeEntity> tasks) {
                super.onPostExecute(tasks);
                arrayList.addAll(tasks);
                initAdapter();
            }
        }

        GetCheques getInvoices = new GetCheques();
        getInvoices.execute();
    }

    private void initAdapter() {
        chequeAdapter = new ChequeAdapter(ChequeActivity.this, arrayList);
        chequeAdapter.notifyDataSetChanged();
        binding.recyclerview.setAdapter(chequeAdapter);

        if (arrayList.size() == 0) {
            binding.RelNo.setVisibility(View.VISIBLE);
        } else {
            binding.RelNo.setVisibility(View.GONE);
        }
    }


    public void populateCheque() {
        arrayList.clear();
        class GetCheque extends AsyncTask<Void, Void, List<ChequeEntity>> {

            @Override
            protected List<ChequeEntity> doInBackground(Void... voids) {
                return DatabaseClient
                        .getInstance(getApplicationContext())
                        .getDatabase()
                        .chequeDAO()
                        .getAll();
            }

            @Override
            protected void onPostExecute(List<ChequeEntity> tasks) {
                super.onPostExecute(tasks);
                arrayList = tasks;
                initAdapter();
            }
        }

        GetCheque getCheque = new GetCheque();
        getCheque.execute();
    }
}