package com.arodi.powergas.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;

import com.arodi.powergas.R;
import com.arodi.powergas.databinding.SalesSummaryBinding;

public class SalesSummary extends AppCompatActivity {
//0766663351
    SalesSummaryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.sales_summary);
        binding.back.setOnClickListener(view -> onBackPressed());
        binding.Sales.setOnClickListener(view -> startActivity(new Intent(SalesSummary.this, TodayActivity.class)));
        binding.Summary.setOnClickListener(view -> startActivity(new Intent(SalesSummary.this, SummaryActivity.class)));

    }
}