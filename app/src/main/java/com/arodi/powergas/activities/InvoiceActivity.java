package com.arodi.powergas.activities;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.InvoiceAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.database.DatabaseClient;
import com.arodi.powergas.databinding.ActivityInvoiceBinding;
import com.arodi.powergas.entities.InvoiceEntity;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.HistoryModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.material.internal.NavigationMenu;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintStream;
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

import org.json.JSONArray;
import org.json.JSONObject;

public class InvoiceActivity extends AppCompatActivity {
    public List<InvoiceEntity> arrayList = new ArrayList<>();
    ActivityInvoiceBinding binding;
    public InvoiceAdapter invoiceAdapter;
    public boolean isLoading = false;
    HashMap<String, String> user;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_invoice);
        binding.back.setOnClickListener(view -> onBackPressed());
        user = new SessionManager(this).getLoginDetails();
        populateInvoice();
        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(this::getInvoice);
        binding.PDFDial.setMenuListener(new SimpleMenuListenerAdapter() {
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            public boolean onMenuItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_customer) {
                    InvoiceActivity.this.startActivity(new Intent(InvoiceActivity.this, CustomerActivity.class));
                    return true;
                } else if (itemId == R.id.action_route) {
                    InvoiceActivity.this.startActivity(new Intent(InvoiceActivity.this, RouteActivity.class));
                    return true;
                } else if (itemId != R.id.action_shop) {
                    return true;
                } else {
                    InvoiceActivity.this.startActivity(new Intent(InvoiceActivity.this, NewCustomerActivity.class));
                    return true;
                }
            }
        });
        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.SearchCustomers.getEditText().addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
            }

            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                if (!s.toString().isEmpty()) {
                    searchInvoice(s.toString());
                }else {
                    populateInvoice();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(binding.SearchCustomers.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }

            }
        });
    }

    public void onResume() {
        super.onResume();
        if (NetworkState.getInstance(this).isConnected()) {
            getInvoice();
        }
    }

    public void getInvoice() {
        binding.Refresh.setRefreshing(true);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "sales/salesEndpoint.php?action=fetch_invoice&salesman_id=" + user.get(SessionManager.KEY_SALESMAN_ID) + "&vehicle_id=" + user.get(SessionManager.KEY_VEHICLE_ID))
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
                    DatabaseClient.getInstance(getApplicationContext()).getDatabase().invoiceDAO().deleteAll();

                    System.out.println("gdhdjhhjcjd"+jsonArray);
                    System.out.println("arrayLenth"+jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        InvoiceEntity entity = new InvoiceEntity(
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
                        DatabaseClient.getInstance(getApplicationContext()).getDatabase().invoiceDAO().insert(entity);
                    }

                    runOnUiThread(() -> {
                        binding.Refresh.setRefreshing(false);
                        populateInvoice();
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    public void searchInvoice(String s) {
        arrayList.clear();
        class GetInvoices extends AsyncTask<Void, Void, List<InvoiceEntity>> {

            @Override
            protected List<InvoiceEntity> doInBackground(Void... voids) {
                return DatabaseClient
                        .getInstance(getApplicationContext())
                        .getDatabase()
                        .invoiceDAO()
                        .searchInvoice(s);
            }

            @Override
            protected void onPostExecute(List<InvoiceEntity> tasks) {
                super.onPostExecute(tasks);
                arrayList = tasks;
                initAdapter();
            }
        }

        GetInvoices getInvoices = new GetInvoices();
        getInvoices.execute();
    }

    private void initAdapter() {
        invoiceAdapter = new InvoiceAdapter(this, arrayList);
        this.binding.recyclerview.setAdapter(this.invoiceAdapter);
        if (arrayList.size() == 0) {
            binding.RelNo.setVisibility(View.VISIBLE);
        } else {
            binding.RelNo.setVisibility(View.GONE);
        }
    }

    public void populateInvoice() {
        arrayList.clear();
        class GetInvoices extends AsyncTask<Void, Void, List<InvoiceEntity>> {

            @Override
            protected List<InvoiceEntity> doInBackground(Void... voids) {
                return DatabaseClient
                        .getInstance(getApplicationContext())
                        .getDatabase()
                        .invoiceDAO()
                        .getAll();
            }

            @Override
            protected void onPostExecute(List<InvoiceEntity> tasks) {
                super.onPostExecute(tasks);
                arrayList = tasks;
                initAdapter();
            }
        }

        GetInvoices getInvoices = new GetInvoices();
        getInvoices.execute();
    }
}
