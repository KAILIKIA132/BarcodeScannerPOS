package com.arodi.powergas.activities;

import static com.arodi.powergas.api.BaseURL.ROUTE_URL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.CustomerAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.background.CustomerWorker;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityMakeSaleBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.CustomerModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.internal.NavigationMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MakeSaleActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    ActivityMakeSaleBinding binding;
    public CustomerAdapter customerAdapter;
    public ArrayList<CustomerModel> arrayList = new ArrayList<>();
    HashMap<String, String> user;
    int day;
    SessionManager session;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    double latitude, longitude = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_make_sale);
        binding.back.setOnClickListener(view -> onBackPressed());
        user = new SessionManager(MakeSaleActivity.this).getLoginDetails();
        session = new SessionManager(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        populateCustomers();
        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(null);
        binding.Refresh.setOnRefreshListener(() -> {
                    if (NetworkState.getInstance(MakeSaleActivity.this).isConnected()) {
                        getCustomers();
                    }
                }
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
                        startActivity(new Intent(MakeSaleActivity.this, CustomerActivity.class));
                        break;
                    case R.id.action_route:
                        startActivity(new Intent(MakeSaleActivity.this, RouteActivity.class));
                        break;
                    case R.id.action_shop:
                        startActivity(new Intent(MakeSaleActivity.this, NewCustomerActivity.class));
                        break;
                }
                return true;
            }
        });

        // Initialize RecyclerView and set GridLayoutManager
        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.setLayoutManager(new GridLayoutManager(MakeSaleActivity.this, 2)); // Adjust span count as needed

        // Initialize and set adapter for RecyclerView
        customerAdapter = new CustomerAdapter(MakeSaleActivity.this, arrayList); // Assuming you have a CustomerAdapter class
        binding.recyclerview.setAdapter(customerAdapter);

        binding.SearchCustomers.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    searchCustomers(s.toString());
                } else {
                    populateCustomers();
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
        if (NetworkState.getInstance(MakeSaleActivity.this).isConnected()) {
            if (!isLocationEnabled(MakeSaleActivity.this)) {
                new AlertDialog.Builder(MakeSaleActivity.this)
                        .setTitle("Location Permissions")
                        .setMessage("To access location, you need to allow location permissions")
                        .setPositiveButton("Allow", (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                        .setCancelable(false)
                        .show();
            }

        }

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            if (NetworkState.getInstance(MakeSaleActivity.this).isConnected()) {
                getCustomers();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MakeSaleActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MakeSaleActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("permission was granted");
                    try {
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, (LocationListener) this);
                    } catch (SecurityException e) {
                        System.out.println("SecurityException" + e);
                    }
                } else {
                    System.out.println("permission denied");
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("onConnectionFailed"+ connectionResult.toString());
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    private void getCustomers() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(ROUTE_URL + "customer.php")
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("errorrr" + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    Log.d("Api_response", jsonObject.toString());

                    if (jsonObject.getInt("status") == 200) {
                        JSONArray customersArray = jsonObject.getJSONArray("data");
                        ArrayList<CustomerModel> customerList = new ArrayList<>();

                        for (int i = 0; i < customersArray.length(); i++) {
                            JSONObject customerObj = customersArray.getJSONObject(i);

                            CustomerModel customer = new CustomerModel(
                                    customerObj.getString("id"),
                                    customerObj.optString("group_name", ""),
                                    customerObj.optString("customer_group_name", ""),
                                    customerObj.optString("name", ""),
                                    customerObj.optString("email", ""),
                                    customerObj.optString("address", ""),
                                    customerObj.optString("phone", ""),
                                    customerObj.optString("city", ""),
                                    customerObj.optString("logo", ""),
                                    customerObj.optString("country", ""),
                                    "uu","io","","66","56"
                            );

                            customerList.add(customer);
                        }

                        runOnUiThread(() -> {
                            arrayList.clear();
                            arrayList.addAll(customerList);
                            initAdapter();
                        });

                    } else {
                        runOnUiThread(() -> {
                            try {
                                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MakeSaleActivity.this, SweetAlertDialog.WARNING_TYPE);
                                sweetAlertDialog.setTitle(jsonObject.getString("message"));
                                sweetAlertDialog.setCancelable(false);
                                sweetAlertDialog.show();
                                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
                                    sweetAlertDialog1.dismiss();
                                    session.logoutUser();
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


//    private void getCustomers() {
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(ROUTE_URL+"customer.php")
//                .method("GET", null)
//                .build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(() -> {
//                    System.out.println("errorrr" + e.toString());
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//                try {
//                    JSONObject jsonObject = new JSONObject(response.body().string());
//
//                    Log.d("Api_response",jsonObject.toString());
//
//                    if (jsonObject.getString("status").equals("200")) {
//                        SharedPreferences sharedPreferences = getSharedPreferences(CustomerWorker.SHARED_PREFERENCE, Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString(CustomerWorker.JSON_ARRAY, jsonObject.getString("data"));
//                        editor.putString(CustomerWorker.LATITUDE, String.valueOf(latitude));
//                        editor.putString(CustomerWorker.LONGITUDE, String.valueOf(longitude));
//                        editor.apply();
//
//                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CustomerWorker.class).build();
//                        WorkManager.getInstance().enqueue(workRequest);
//
//                        runOnUiThread(() -> {
//                            if (MakeSaleActivity.this != null) {
//                                if (workRequest != null) {
//                                    WorkManager.getInstance().getWorkInfoByIdLiveData(workRequest.getId()).observe(MakeSaleActivity.this, workInfo -> {
//                                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
//                                            populateCustomers();
//                                        }
//                                    });
//                                }
//                            }
//                        });
//
//                    } else if (jsonObject.getString("success").equals("2")) {
//                        runOnUiThread(() -> {
//                            try {
//                                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MakeSaleActivity.this, SweetAlertDialog.WARNING_TYPE);
//                                sweetAlertDialog.setTitle(jsonObject.getString("message"));
//                                sweetAlertDialog.setCancelable(false);
//                                sweetAlertDialog.show();
//                                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
//                                    sweetAlertDialog1.dismiss();
//                                    session.logoutUser();
//                                });
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        });
//                    }
//
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//
//            }
//        });
//    }

    public void searchCustomers(String s) {
        arrayList.clear();
        arrayList = (ArrayList<CustomerModel>) new Database(getBaseContext()).search_customers(s);
        initAdapter();
    }


    private void initAdapter() {
        customerAdapter = new CustomerAdapter(MakeSaleActivity.this, arrayList);
        binding.recyclerview.setAdapter(customerAdapter);
        customerAdapter.notifyDataSetChanged();

        if (arrayList.size() == 0) {
            binding.RelNo.setVisibility(View.VISIBLE);
        } else {
            binding.RelNo.setVisibility(View.GONE);
        }
    }


    public void populateCustomers() {
        arrayList.clear();
        arrayList = (ArrayList<CustomerModel>) new Database(MakeSaleActivity.this.getBaseContext()).fetch_customers();
        initAdapter();
    }

}