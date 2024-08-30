package com.arodi.powergas.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.arodi.powergas.R;
import com.arodi.powergas.databinding.ActivitySplashBinding;
import com.arodi.powergas.helpers.NetworkState;


public class Splash extends AppCompatActivity {
    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        
        Animation anim = AnimationUtils.loadAnimation(this,R.anim.splash_animation);
        binding.SplashImage.startAnimation(anim);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, Login.class));
            Splash.this.finish();
        }, 2500);
        

        
    }

}
