package com.arodi.powergas.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.StockAdapter;
import com.arodi.powergas.adapters.TodayAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityStockBinding;
import com.arodi.powergas.databinding.ActivityTodayBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.ProductModel;
import com.arodi.powergas.models.ProductQuantity;
import com.arodi.powergas.models.StockModel;
import com.arodi.powergas.models.TodayModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TodayActivity extends AppCompatActivity {
    ActivityTodayBinding binding;
    TodayAdapter adapter;
    ArrayList<TodayModel> todayModel = new ArrayList<>();
    HashMap<String, String> user;
    SimpleDateFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_today);
        user = new SessionManager(TodayActivity.this).getLoginDetails();
        binding.back.setOnClickListener(view -> onBackPressed());
        binding.RecyclerView.setHasFixedSize(true);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(TodayActivity.this));

        format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(this::getToday);
        binding.Start.setText(format.format(new Date()));
        binding.End.setText(format.format(new Date()));

        binding.Start.setOnClickListener(v -> DateTimePicker(binding.Start));
        binding.End.setOnClickListener(v -> DateTimePicker(binding.End));
        binding.Submit.setOnClickListener(v -> getToday());
        binding.Close.setOnClickListener(v -> closeStock());

        binding.SalesFor.setText("Total Sales - "+user.get(SessionManager.KEY_PLATE_NO));

        populateToday();
    }

    private void DateTimePicker(EditText editText) {
        final Calendar currentDate = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, monthOfYear, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            editText.setText(format.format(calendar.getTime()));
        };
        DatePickerDialog datePickerDialog = new
                DatePickerDialog(TodayActivity.this, dateSetListener,
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkState.getInstance(TodayActivity.this).isConnected()) {
            getToday();
        }
    }

    private void getToday() {
        binding.Refresh.setRefreshing(true);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("vehicle_id", user.get(SessionManager.KEY_VEHICLE_ID))
                .addFormDataPart("distributor_id", user.get(SessionManager.KEY_DISTRIBUTOR_ID))
                .addFormDataPart("start_date", binding.Start.getText().toString().trim())
                .addFormDataPart("end_date", binding.End.getText().toString().trim())
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL+"getCurrentSoldItems")
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
                        System.out.println("jsonObjectiiddddiii" + jsonArray);

                        new Database(getBaseContext()).clear_today();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            TodayModel model = new TodayModel(
                                    jsonObject.getString("id"),
                                    jsonObject.getString("name"),
                                    jsonObject.getString("soldQty"),
                                    jsonObject.getString("totalSale"));

                            new Database(getBaseContext()).create_today(model);
                        }
                        populateToday();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    private void closeStock() {
        SweetAlertDialog dialog = new SweetAlertDialog(TodayActivity.this, SweetAlertDialog.PROGRESS_TYPE);
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
                .addFormDataPart("vehicle_id", user.get(SessionManager.KEY_VEHICLE_ID))
                .addFormDataPart("distributor_id", user.get(SessionManager.KEY_DISTRIBUTOR_ID))
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "closeStock")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("errorrr" + e.toString());
                    Toast.makeText(TodayActivity.this, "No connection to host", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        if (jsonObject.getString("success").equals("1")) {
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            dialog.setTitle(jsonObject.getString("message"));
                            dialog.setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismiss();
                                onBackPressed();
                            });
                        }else {
                            Toast.makeText(TodayActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        dialog.dismiss();
                        Toast.makeText(TodayActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    private void initAdapter() {
        adapter = new TodayAdapter(TodayActivity.this, todayModel);
        adapter.notifyDataSetChanged();
        binding.RecyclerView.setAdapter(adapter);

        double total = 0.0;
        try {
            for (TodayModel model : todayModel) {
                total += Double.parseDouble(model.getTotal());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        binding.TotalItems.setText("KES "+String.valueOf(Math.round(total)));

        if (todayModel.size() == 0) {
            binding.RelNo.setVisibility(View.VISIBLE);
            binding.Head.setVisibility(View.GONE);
        } else {
            binding.RelNo.setVisibility(View.GONE);
            binding.Head.setVisibility(View.VISIBLE);
        }
    }

    public void populateToday() {
        todayModel.clear();
        todayModel = (ArrayList<TodayModel>) new Database(TodayActivity.this).fetch_today();
        initAdapter();
    }
}