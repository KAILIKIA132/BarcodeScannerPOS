package com.arodi.powergas.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.arodi.powergas.R;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivitySaleBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.CustomerModel;
import com.arodi.powergas.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SaleActivity extends AppCompatActivity {
    
    ActivitySaleBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sale);

        binding.TakeOrder.setOnClickListener(v -> Toast.makeText(SaleActivity.this, "Coming soon", Toast.LENGTH_SHORT).show());
        binding.back.setOnClickListener(view -> onBackPressed());
        binding.MakeSale.setOnClickListener(view -> startActivity(new Intent(SaleActivity.this, MakeSaleActivity.class)));
        binding.AddCustomer.setOnClickListener(view -> startActivity(new Intent(SaleActivity.this, CustomerActivity.class)));
        binding.walkIn.setOnClickListener(view -> {
            Intent intent = new Intent(SaleActivity.this, ShopActivity.class);
            startActivity(intent);
            // Save data in SharedPreferences
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CustomerPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("SELECTED_CUSTOMER_NAME", "Walk In Customer");
            editor.apply(); // Don't forget to apply the changes

            // Start the new activity

        });




    }




}