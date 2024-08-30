package com.arodi.powergas.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arodi.powergas.R;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityNewShopBinding;
import com.arodi.powergas.models.CustomerModel;
import com.arodi.powergas.models.NewCustomerModel;
import com.arodi.powergas.models.ProductModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewShopActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    ActivityNewShopBinding binding;
    public ArrayList<CustomerModel> arrayList = new ArrayList<>();
    ArrayList<String> list = new ArrayList<>();
    String customer_id;
    Bitmap bitmap;
    HashMap<String, String> user;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    double latitude, longitude = 0;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_shop);
        populateCustomers();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        binding.back.setOnClickListener(v -> onBackPressed());
        user = new SessionManager(NewShopActivity.this).getLoginDetails();

        binding.Customers.setOnClickListener(v -> {
            dialog = new Dialog(NewShopActivity.this, R.style.DialogTheme);
            if (!dialog.isShowing()) {

                dialog.setContentView(R.layout.dialog_spinner);

                dialog.show();

                TextInputLayout textInputLayout = dialog.findViewById(R.id.Search);
                ListView listView = dialog.findViewById(R.id.ItemList);

                TextView close = dialog.findViewById(R.id.Close);
                close.setOnClickListener(v1 -> dialog.dismiss());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(NewShopActivity.this, android.R.layout.simple_list_item_1, list);

                listView.setAdapter(adapter);

                textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    binding.Customers.setText(adapter.getItem(position));

                    int i = list.indexOf(adapter.getItem(position));

                    CustomerModel model = arrayList.get(i);
                    customer_id = model.getCustomer_id();

                    dialog.dismiss();
                });
            }
        });



        binding.Gallery.setOnClickListener(view -> {
            Toast.makeText(NewShopActivity.this, "Not available please use camera", Toast.LENGTH_SHORT).show();
        });

        binding.Camera.setOnClickListener(view -> {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(NewShopActivity.this.getPackageManager()) != null) {
                startActivityForResult(i, 480);
            }
        });

        binding.confirmButton.setOnClickListener(v -> {
            if (validate()) {
                addShop();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLocationEnabled(NewShopActivity.this)) {
            new AlertDialog.Builder(NewShopActivity.this)
                    .setTitle("Location Permissions")
                    .setMessage("To add shop, you need to allow location permissions")
                    .setPositiveButton("Allow", (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setCancelable(false)
                    .show();
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

            ActivityCompat.requestPermissions(NewShopActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, NewShopActivity.this);

    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("permission was granted");
                    try {
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
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
        System.out.println("onConnectionFailed" + connectionResult.toString());
    }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == 480 && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                bitmap = (Bitmap) data.getExtras().get("data");
                binding.Image.setImageBitmap(bitmap);
            }
        }


    }


    public boolean validate() {
        boolean valid = true;

        if (bitmap == null) {
            Toast.makeText(NewShopActivity.this, "Please Capture Building Picture", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (customer_id == null) {
            Toast.makeText(NewShopActivity.this, "Please Select Customer", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (binding.ShopName.getEditText().getText().toString().trim().equals("")) {
            binding.ShopName.setError("Please input Shop Name");
            valid = false;
        } else {
            binding.ShopName.setError("");
        }

        return valid;
    }

    private void addShop() {
        final SweetAlertDialog dialog = new SweetAlertDialog(NewShopActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitle("Please Wait...");
        dialog.setCancelable(false);
        dialog.show();


        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("customer_id", customer_id);
            jsonObject.put("lat", latitude);
            jsonObject.put("lng", longitude);
            jsonObject.put("route_id", user.get(SessionManager.KEY_ROUTE_ID));
            jsonObject.put("image", getStringImage(bitmap));
            jsonObject.put("distributor_id", user.get(SessionManager.KEY_DISTRIBUTOR_ID));
            jsonObject.put("salesman_id", user.get(SessionManager.KEY_SALESMAN_ID));
            jsonObject.put("shop_name", binding.ShopName.getEditText().getText().toString().trim());
            jsonArray.put(jsonObject);

            System.out.println("fhdghhhdhsd" + jsonArray);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonArray.toString());

            Request request = new Request.Builder()
                    .url(BaseURL.SERVER_URL + "customers/?action=add_shop")
                    .method("POST", body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(NewShopActivity.this, "No connection to host", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        System.out.println("errorrr" + e.toString());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject object = new JSONObject(response.body().string());

                            if (object.getString("success").equals("1")) {
                                dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                dialog.setTitle(object.getString("message"));
                                dialog.setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismiss();
                                    onBackPressed();
                                });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(NewShopActivity.this, object.getString("message"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            dialog.dismiss();

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("errors");
                            myRef.setValue(e.toString());

                            Toast.makeText(NewShopActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }


                    });

                }
            });
        } catch (Exception e) {
            dialog.dismiss();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("errors");
            myRef.setValue(e.toString());

            Toast.makeText(NewShopActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }


    public String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByteArray, Base64.DEFAULT);
    }

    public void populateCustomers() {
        arrayList.clear();
        arrayList = (ArrayList<CustomerModel>) new Database(NewShopActivity.this.getBaseContext()).fetch_all_customers();
        for (int i = 0; i < arrayList.size(); i++) {
            String name = arrayList.get(i).getName();
            list.add(name);

        }
        System.out.println("terffffff" + list);
        binding.LoadCustomers.setVisibility(View.GONE);

        if (list.size() == 0) {
            binding.LoadCustomers.setVisibility(View.VISIBLE);
            binding.LoadCustomers.setText("No customer available!");
        } else {
            binding.LoadCustomers.setVisibility(View.GONE);
        }
    }
}