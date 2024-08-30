package com.arodi.powergas.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.EditText;

import com.arodi.powergas.R;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.databinding.ActivitySummaryBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.session.SessionManager;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SummaryActivity extends AppCompatActivity {
//    ActivitySummaryBinding binding;
    ActivitySummaryBinding binding;
    HashMap<String, String> user;
    SimpleDateFormat format;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_summary);

        user = new SessionManager(SummaryActivity.this).getLoginDetails();
        binding.back.setOnClickListener(view -> onBackPressed());

        format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(this::getToday);
        binding.Start.setText(format.format(new Date()));
        binding.End.setText(format.format(new Date()));

        binding.Start.setOnClickListener(v -> DateTimePicker(binding.Start));
        binding.End.setOnClickListener(v -> DateTimePicker(binding.End));
        binding.Submit.setOnClickListener(v -> getToday());

        binding.SalesFor.setText("Summary - "+user.get(SessionManager.KEY_PLATE_NO));
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
                DatePickerDialog(SummaryActivity.this, dateSetListener,
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkState.getInstance(SummaryActivity.this).isConnected()) {
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
                .url(BaseURL.ROUTE_URL + "")
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
//                        JSONObject jsonObject = new JSONObject(response.body().string());
                        Log.i("server response","jsonObjfgectiiddddiii" + response.body().string());

                    //display data on ui.

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                });

            }
        });
    }
}