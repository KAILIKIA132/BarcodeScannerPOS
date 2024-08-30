package com.arodi.powergas.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.arodi.powergas.R;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.databinding.ActivityCustomerBinding;
import com.arodi.powergas.models.CategoryModel;
import com.arodi.powergas.models.CityModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
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

public class CustomerActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    ActivityCustomerBinding binding;

    public String selected_town_id, selected_county_id;
    ArrayList<CityModel> cityModel = new ArrayList<>();
    ArrayList<String> cityList = new ArrayList<>();

    public String selected_group_name, selected_group_id;
    ArrayList<CategoryModel> categoryModel = new ArrayList<>();
    ArrayList<String> categoryList = new ArrayList<>();
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    double latitude, longitude = 0;

    SessionManager session;
    HashMap<String, String> user;

    Bitmap bitmap;
    SweetAlertDialog sweetAlertDialog;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_customer);
        binding.back.setOnClickListener(view -> onBackPressed());

        session = new SessionManager(CustomerActivity.this);
        user = new SessionManager(CustomerActivity.this).getLoginDetails();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        getTowns();
        getCategory();

        binding.Town.setOnClickListener(v -> {
            dialog = new Dialog(CustomerActivity.this, R.style.DialogTheme);
            if (!dialog.isShowing()) {

                dialog.setContentView(R.layout.dialog_spinner);

                dialog.show();

                TextInputLayout textInputLayout = dialog.findViewById(R.id.Search);
                ListView listView = dialog.findViewById(R.id.ItemList);

                TextView close = dialog.findViewById(R.id.Close);
                close.setOnClickListener(v1 -> dialog.dismiss());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CustomerActivity.this, android.R.layout.simple_list_item_1, cityList);

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
                    binding.Town.setText(adapter.getItem(position));

                    int i = cityList.indexOf(adapter.getItem(position));
                    CityModel model = cityModel.get(i);
                    selected_town_id = model.getId();
                    selected_county_id = model.getCounty_id();

                    dialog.dismiss();
                });
            }
        });

        binding.Category.setOnClickListener(v -> {
            dialog = new Dialog(CustomerActivity.this, R.style.DialogTheme);
            if (!dialog.isShowing()) {

                dialog.setContentView(R.layout.dialog_spinner);

                dialog.show();

                TextInputLayout textInputLayout = dialog.findViewById(R.id.Search);
                ListView listView = dialog.findViewById(R.id.ItemList);

                TextView close = dialog.findViewById(R.id.Close);
                close.setOnClickListener(v1 -> dialog.dismiss());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CustomerActivity.this, android.R.layout.simple_list_item_1, categoryList);

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
                    binding.Category.setText(adapter.getItem(position));

                    int i = categoryList.indexOf(adapter.getItem(position));
                    CategoryModel model = categoryModel.get(i);
                    selected_group_id = model.getId();
                    selected_group_name = model.getName();

                    dialog.dismiss();
                });
            }
        });

        binding.Gallery.setOnClickListener(view -> {
            Toast.makeText(CustomerActivity.this, "Not available please use camera", Toast.LENGTH_SHORT).show();
        });

        binding.Camera.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(CustomerActivity.this.getPackageManager()) != null) {
                startActivityForResult(intent, 480);
            }
        });

        binding.SubmitButton.setOnClickListener(view -> {
            if (validate()) {
                sweetAlertDialog = new SweetAlertDialog(CustomerActivity.this, SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog.setTitle("Please confirm phone number!\n" + binding.MobileNumber.getEditText().getText().toString().trim());
                sweetAlertDialog.show();
                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> registerCustomer());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLocationEnabled(CustomerActivity.this)) {
            new AlertDialog.Builder(CustomerActivity.this)
                    .setTitle("Location Permissions")
                    .setMessage("To add customer, you need to allow location permissions")
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

            ActivityCompat.requestPermissions(CustomerActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, CustomerActivity.this);

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
        if (requestCode == 480 && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            binding.Image.setImageBitmap(bitmap);
        }


    }

    private void getTowns() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "customers/customersEndpoint.php?action=fetch_towns")
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> System.out.println("errorrr" + e.toString()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        System.out.println("hdhvdgvdgdgd" + jsonArray);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            CityModel model = new CityModel(
                                    jsonObject.getString("id"),
                                    jsonObject.getString("city"),
                                    jsonObject.getString("county_id"),
                                    jsonObject.getString("county_name"));
                            cityModel.add(model);
                        }

                        for (int i = 0; i < cityModel.size(); i++) {
                            String name = cityModel.get(i).getCity();
                            String county = cityModel.get(i).getCounty_name();
                            cityList.add(name + "  (" + county + ") ");
                        }
                        binding.LoadTowns.setVisibility(View.GONE);

                        if (cityList.size() == 0) {
                            binding.LoadTowns.setVisibility(View.VISIBLE);
                            binding.LoadTowns.setText("No town available!");
                        } else {
                            binding.LoadTowns.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        System.out.println("hgsgdgdgdgdgd" + e);
                        Toast.makeText(CustomerActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    private void getCategory() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
//                .url(BaseURL.SERVER_URL + "customers/customersEndpoint.php?action=customer_groups")
                .url(BaseURL.SERVER_URL + "customers/customersEndpoint.php?action=customer_groups")
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> System.out.println("errorrr" + e.toString()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            CategoryModel model = new CategoryModel(
                                    jsonObject.getString("id"),
                                    jsonObject.getString("name"));
                            categoryModel.add(model);
                        }

                        for (int i = 0; i < categoryModel.size(); i++) {
                            String name = categoryModel.get(i).getName();
                            categoryList.add(name);
                        }
                        binding.LoadCategory.setVisibility(View.GONE);

                        if (categoryList.size() == 0) {
                            binding.LoadCategory.setVisibility(View.VISIBLE);
                            binding.LoadCategory.setText("No category available!");
                        } else {
                            binding.LoadCategory.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        Toast.makeText(CustomerActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });

            }
        });

    }

    private void registerCustomer() {
        sweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.setTitle("Please Wait...");

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("group_id", selected_group_id);
            jsonObject.put("group_name", selected_group_name);
            jsonObject.put("name", binding.FirstName.getEditText().getText().toString().trim() + " " + binding.LastName.getEditText().getText().toString().trim());
            jsonObject.put("country", selected_county_id);
            jsonObject.put("email", binding.EmailAddress.getEditText().getText().toString().trim());
            jsonObject.put("phone", binding.MobileNumber.getEditText().getText().toString().trim());
            jsonObject.put("phone_2", binding.WhatsAppNumber.getEditText().getText().toString().trim());
            jsonObject.put("logo", getStringImage(bitmap));
            jsonObject.put("lat", latitude);
            jsonObject.put("lng", longitude);
            jsonObject.put("town_id", selected_town_id);
            jsonObject.put("shop_name", binding.ShopName.getEditText().getText().toString().trim());
            jsonObject.put("route_id", user.get(SessionManager.KEY_ROUTE_ID));
            jsonObject.put("distributor_id", user.get(SessionManager.KEY_DISTRIBUTOR_ID));
            jsonObject.put("salesman_id", user.get(SessionManager.KEY_SALESMAN_ID));


            jsonArray.put(jsonObject);

            System.out.println("data to the server " + jsonArray);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonArray.toString());

            Request request = new Request.Builder()
                    .url(BaseURL.SERVER_URL + "customers/customersEndpoint.php?action=register_customers")
                    .method("POST", body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(CustomerActivity.this, "No connection to host", Toast.LENGTH_SHORT).show();
                        sweetAlertDialog.dismiss();
                        System.out.println("errorrr" + e.toString());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject object = new JSONObject(response.body().string());
                            System.out.println("hfhfhfhfhf" + object);

                            if (object.getString("success").equals("1")) {
                                sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                sweetAlertDialog.setTitle(object.getString("message"));
                                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismiss();
                                    onBackPressed();
                                });
                            } else {
                                sweetAlertDialog.dismiss();
                                Toast.makeText(CustomerActivity.this, object.getString("message"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            sweetAlertDialog.dismiss();
                            e.printStackTrace();
                            System.out.println("fhfhfhjfjfg" + e);
                            Toast.makeText(CustomerActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }


                    });
                }
            });
        } catch (Exception e) {
            sweetAlertDialog.dismiss();
            System.out.println("errrrrorrr" + e.toString());
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("errors");
            myRef.setValue(e.toString());
            Toast.makeText(CustomerActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    public String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByteArray, Base64.DEFAULT);
    }

    public boolean validate() {
        boolean valid = true;
        if (bitmap == null) {
            Toast.makeText(CustomerActivity.this, "Please Capture Building Picture", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (selected_group_id == null) {
            Toast.makeText(CustomerActivity.this, "Please Select Customer Category", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (binding.ShopName.getEditText().getText().toString().trim().equals("")) {
            binding.ShopName.setError("Please input Shop Name");
            valid = false;
        } else {
            binding.ShopName.setError("");
        }

        if (binding.FirstName.getEditText().getText().toString().trim().equals("")) {
            binding.FirstName.setError("Please input Customer First Name");
            valid = false;
        } else {
            binding.FirstName.setError("");
        }

        if (binding.LastName.getEditText().getText().toString().trim().equals("")) {
            binding.LastName.setError("Please input Customer Last Name");
            valid = false;
        } else {
            binding.LastName.setError("");
        }

        if (binding.MobileNumber.getEditText().getText().toString().trim().equals("") || binding.MobileNumber.getEditText().getText().toString().trim().length() != 10) {
            binding.MobileNumber.setError("Please input valid Mobile Number");
            valid = false;
        } else {
            binding.MobileNumber.setError("");
        }

//        if (selected_town_id == null) {
//            Toast.makeText(CustomerActivity.this, "Please Select Town", Toast.LENGTH_SHORT).show();
//            valid = false;
//        }

        if (selected_town_id == null) {
            // Assign a default value
            selected_town_id = "1"; // Replace "default_town_id" with your desired default value

            // Show Toast message
//            Toast.makeText(CustomerActivity.this, "Please Select Town", Toast.LENGTH_SHORT).show();
            valid = true;
        }
        return valid;
    }
}