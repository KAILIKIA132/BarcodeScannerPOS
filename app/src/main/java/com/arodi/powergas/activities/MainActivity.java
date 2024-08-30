package com.arodi.powergas.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.arodi.powergas.R;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityMainBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.ProductModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    int day;
    HashMap<String, String> user;
    SessionManager session;
    boolean doubleBackToExitPressedOnce = false;
    public int MY_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        session = new SessionManager(MainActivity.this);
        checkPermissions();
        user = new SessionManager(MainActivity.this).getLoginDetails();
        binding.MakeSale.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SaleActivity.class)));
        binding.Expenditure.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ExpenditureActivity.class)));
        binding.Account.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AccountActivity.class)));
        binding.Stock.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, StockActivity.class)));
        binding.SaleHistory.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SaleHistory.class)));
        binding.Sales.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SalesSummary.class)));
        binding.Discount.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DiscountActivity.class)));
        binding.Invoice.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, InvoiceActivity.class)));
        binding.Cheque.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChequeActivity.class)));
        getProducts();

        binding.ExitApp.setOnClickListener(v -> {
            SweetAlertDialog dialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
            dialog.setTitle("You are about to logout!");
            dialog.show();
            dialog.setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                session.logoutUser();
            });
        });

        updateApk();

    }

    public void updateApk() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                System.out.println("update complete Result code: " + resultCode);
            } else {
                System.out.println("update failed Result code: " + resultCode);
            }
        }
    }


    private void getProducts() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        System.out.println("dayofweek" + dayOfWeek);

        if (dayOfWeek != null) {
            switch (dayOfWeek) {
                case "Monday":
                    day = 1;
                    break;
                case "Tuesday":
                    day = 2;
                    break;
                case "Wednesday":
                    day = 3;
                    break;
                case "Thursday":
                    day = 4;
                    break;
                case "Friday":
                    day = 5;
                    break;
                case "Saturday":
                    day = 6;
                    break;
                case "Sunday":
                    day = 7;
                    break;
            }
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "products/productsEndpoint.php?action=fetch_products&vehicle_id=" + user.get(SessionManager.KEY_VEHICLE_ID) + "&day=" + day + "&distributor_id=" + user.get(SessionManager.KEY_DISTRIBUTOR_ID))
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> System.out.println("errorrr" + e.toString()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                MainActivity.this.runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        System.out.println("jsonObjectiiddddiii" + jsonArray);

                        new Database(MainActivity.this.getBaseContext()).clear_products();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            ProductModel model = new ProductModel(
                                    jsonObject.getString("product_id"),
                                    jsonObject.getString("product_code"),
                                    jsonObject.getString("product_name"),
                                    jsonObject.getString("price"),
                                    jsonObject.getString("quantity"),
                                    jsonObject.getString("plate_no"),
                                    jsonObject.getString("discount_enabled"),
                                    jsonObject.getString("target"),
                                    jsonObject.getString("portion1"),
                                    jsonObject.getString("portion1qty"),
                                    jsonObject.getString("portion2"),
                                    jsonObject.getString("portion2qty"),
                                    jsonObject.getString("portion3"),
                                    jsonObject.getString("portion3qty"),
                                    jsonObject.getString("portion4"),
                                    jsonObject.getString("portion4qty"),
                                    jsonObject.getString("portion5"),
                                    jsonObject.getString("portion5qty"),
                                    jsonObject.getString("isKitchen"));

                            new Database(getBaseContext()).create_product(model);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, "Press back again to logout", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3500);
    }

    public void checkPermissions() {
        Dexter.withActivity(MainActivity.this)
                .withPermissions(Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(MainActivity.this, "Error occurred!" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .check();
    }
}