package com.arodi.powergas.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.StrictMode;

import com.arodi.powergas.BuildConfig;
import com.arodi.powergas.R;
import com.arodi.powergas.databinding.ActivityAccountBinding;
import com.arodi.powergas.session.SessionManager;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AccountActivity extends AppCompatActivity {

    ActivityAccountBinding binding;
    HashMap<String, String> user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account);
        user = new SessionManager(AccountActivity.this).getLoginDetails();

        Picasso.with(AccountActivity.this).load(user.get(SessionManager.KEY_AVATAR)).into(binding.Image);

        binding.back.setOnClickListener(v -> onBackPressed());
        binding.UserName.setText(user.get(SessionManager.KEY_USERNAME));
        binding.EmailAddress.setText(user.get(SessionManager.KEY_EMAIL));
        binding.PhoneNumber.setText(user.get(SessionManager.KEY_PHONE));
        binding.FirstName.setText(user.get(SessionManager.KEY_FIRST_NAME));
        binding.LastName.setText(user.get(SessionManager.KEY_LAST_NAME));
        binding.NumberPlate.setText(user.get(SessionManager.KEY_PLATE_NO));
        binding.DiscountEnabled.setText(user.get(SessionManager.KEY_DISCOUNT_ENABLED));

        binding.Version.setText("Version: " + BuildConfig.VERSION_NAME);
    }
}