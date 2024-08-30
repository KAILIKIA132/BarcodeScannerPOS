package com.arodi.powergas.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.StrictMode;

import com.arodi.powergas.R;
import com.arodi.powergas.databinding.ActivityExpenseDetailBinding;
import com.arodi.powergas.models.ExpenditureModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpenseDetailActivity extends AppCompatActivity {
    ActivityExpenseDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_expense_detail);

        ExpenditureModel model = getIntent().getParcelableExtra("EXPENSE_DETAIL");
        binding.back.setOnClickListener(v -> onBackPressed());
        binding.Date.setText(model.getDate());
        binding.Name.setText(model.getReference());
        binding.RequestedAmount.setText(model.getAmount());
        binding.ApprovedAmount.setText(model.getApproved());
        binding.Comments.setText(model.getNote());
        binding.Status.setText(model.getStatus());

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(model.getDate());
            if (date!=null) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                binding.Day.setText(String.valueOf(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}