package com.arodi.powergas.activities;

import static com.arodi.powergas.api.BaseURL.ROUTE_URL;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.PaymentAdapter;
import com.arodi.powergas.adapters.SaleAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityShopBinding;
import com.arodi.powergas.databinding.AttendDialogBinding;
import com.arodi.powergas.databinding.PaymentDialogBinding;
import com.arodi.powergas.databinding.ProductDialogBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.interfaces.shopInterface;
import com.arodi.powergas.models.PaymentModel;
import com.arodi.powergas.models.ProductModel;
import com.arodi.powergas.models.SaleModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShopActivityThree extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, shopInterface {
    ActivityShopBinding binding;
    ProductDialogBinding dialogBinding;
    AttendDialogBinding attendBinding;
    ArrayList<ProductModel> arrayList = new ArrayList<>();
    ArrayList<String> list = new ArrayList<>();
    PaymentDialogBinding paymentBinding;
    ArrayList<SaleModel> saleModel = new ArrayList<>();
    SaleAdapter saleAdapter;
    public String product_id, name, price, product_code, is_kitchen;
    public String portion1, portion1qty, portion2, portion2qty, portion3, portion3qty, portion4, portion4qty, portion5, portion5qty;

    HashMap<String, String> user;
    int day;
    int final_price;
    int sub_total;

    int total_payment;
    int total_discount;

    Dialog dialog;

    public String positive_negative_discount = "1";
    public int positive_negative_normal = 1;

    public PaymentAdapter paymentAdapter;
    public ArrayList<PaymentModel> paymentModel = new ArrayList<>();

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    double latitude, longitude = 0;
    SharedPreferences sharedPreferences;

    SweetAlertDialog sweetAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop);
        user = new SessionManager(ShopActivityThree.this).getLoginDetails();




        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        sharedPreferences = getSharedPreferences("CUSTOMER_SHOPPING_DATA", Context.MODE_PRIVATE);

        binding.AddProduct.setOnClickListener(view -> showDialog());

        binding.NotAttending.setOnClickListener(v -> {
            Location current = new Location(LocationManager.GPS_PROVIDER);
            current.setLatitude(latitude);
            current.setLongitude(longitude);

            Location customer = new Location(LocationManager.GPS_PROVIDER);
            customer.setLatitude(Double.parseDouble(sharedPreferences.getString("LAT", "")));
            customer.setLongitude(Double.parseDouble(sharedPreferences.getString("LNG", "")));
            binding.NotAttending.setChecked(true);

            if (Double.parseDouble(new DecimalFormat("0.00").format(current.distanceTo(customer) / 1000)) <= 500000) {
                attendDialog();
            }else {
                binding.NotAttending.setChecked(false);
                Toast.makeText(ShopActivityThree.this, "Please move closer to the customer shop", Toast.LENGTH_SHORT).show();
            }
        });

        binding.Payment.setOnClickListener(view -> {
            if (saleModel.size() != 0) {
                sweetAlertDialog = new SweetAlertDialog(ShopActivityThree.this, SweetAlertDialog.PROGRESS_TYPE);
                sweetAlertDialog.setTitle("Please Wait...");
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.show();
                if (total_discount == 0) {
                    makeSale();
                } else {
                    submitDiscount();
                }

            } else {
                Toast.makeText(ShopActivityThree.this, "No sale item available!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.RecyclerView.setHasFixedSize(true);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(ShopActivityThree.this));

        getProducts();

    }

    public static String removeFirstandLast(String str) {
        str = str.substring(1, str.length() - 1);
        return str;
    }


    public void makeSale() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL_NEW + "getPaymentMethodsJson/" + sharedPreferences.getString("CUSTOMER_ID", ""))
                .method("GET", null)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    sweetAlertDialog.dismiss();
                    Toast.makeText(ShopActivityThree.this, "No Connection to host", Toast.LENGTH_SHORT).show();
                    System.out.println("errorrr" + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    sweetAlertDialog.dismiss();
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        System.out.println("jsonObjectiiiii" + jsonArray);

                        showPayment(jsonArray);

                    } catch (Exception e) {
                        System.out.println("nhdhdhdhdd" + e);
                        Log.d("Error on payment1:",e.toString());
                        Toast.makeText(ShopActivityThree.this, "An error occurred", Toast.LENGTH_LONG).show();
                    }


                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateSales();

        if (NetworkState.getInstance(ShopActivityThree.this).isConnected()) {
            if (!isLocationEnabled(ShopActivityThree.this)) {
                new AlertDialog.Builder(ShopActivityThree.this)
                        .setTitle("Location Permissions")
                        .setMessage("To access location, you need to allow location permissions")
                        .setPositiveButton("Allow", (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                        .setCancelable(false)
                        .show();
            }

            getProducts();
        }
    }

    private void getProducts() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        Log.d("getProducts", "Day of Week: " + dayOfWeek);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();


        Request request = new Request.Builder()
                .url(ROUTE_URL + "productsApi.php")
                .method("GET", null)
                .build();

        Log.d("Api_request",request.toString());

        Log.d("getProducts", "Sending request...");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.e("getProducts", "Request failed: " + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("getProducts", "Response received: " + response.toString());

                if (!response.isSuccessful()) {
                    Log.e("getProducts", "Unexpected response code: " + response.code());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                Log.d("getProducts", "Response Body: " + responseBody);

                if (responseBody.isEmpty()) {
                    Log.w("getProducts", "Empty response body");
                    return;
                }

                // Process the response in a background thread
                new Thread(() -> {
                    try {
                        Log.d("getProducts", "Processing response...");
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray jsonArray = jsonResponse.getJSONArray("data");
                        Log.d("getProducts", "JSON Array Length: " + jsonArray.length());

                        List<ProductModel> productList = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Log.d("getProducts", "Processing item: " + i);

                            ProductModel model = new ProductModel(
                                    jsonObject.getString("id"),
                                    jsonObject.getString("code"),
                                    jsonObject.getString("name"),
                                    jsonObject.getString("price"),
                                    jsonObject.getString("stock"),
                                    jsonObject.optString("image", "no_image.png"),
                                    jsonObject.getString("category"),
                                    jsonObject.getString("category_id"),
                                    jsonObject.getString("type"),
                                    jsonObject.optString("sales", "0.0000"),
                                    jsonObject.optString("quantity", "0.0000"),
                                    "4", "8", "9", "9", "9", "8", "3","3"
                            );

                            Log.d("getProducts", "Product added: " + model.getProduct_name());
                            productList.add(model);
                        }

                        Log.d("getProducts", "Total products processed: " + productList.size());

                        // Update the UI with the product data on the main thread
                        runOnUiThread(() -> {
                            Log.d("getProducts", "Updating UI with products");
                            populateProducts(productList);
                        });

                    } catch (Exception e) {
                        Log.e("getProducts", "Error processing response: ", e);
                    }
                }).start();
            }
        });
    }







//    private void fetchProductQuantity() {
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(BaseURL.SERVER_URL + "products/productsEndpoint.php?action=fetch_product_quantity&vehicle_id=" + user.get(SessionManager.KEY_VEHICLE_ID) + "&distributor_id=" + user.get(SessionManager.KEY_DISTRIBUTOR_ID))
//                .method("GET", null)
//                .build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(() -> System.out.println("errorrr" + e.toString()));
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//                runOnUiThread(() -> {
//                    try {
//                        JSONArray jsonArray = new JSONArray(response.body().string());
//                        System.out.println("jsonObjuyrtygtyectdfdfdiighgghhddddiii" + jsonArray);
//
//                        new Database(ShopActivity.this.getBaseContext()).clear_product_quantity();
//
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            ProductQuantity model = new ProductQuantity(
//                                    jsonObject.getString("product_id"),
//                                    jsonObject.getString("quantity"));
//
//                            new Database(getBaseContext()).create_product_quantity(model);
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//
//                });
//
//            }
//        });
//    }


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

            ActivityCompat.requestPermissions(ShopActivityThree.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ShopActivityThree.this);
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
                                mGoogleApiClient, mLocationRequest, this);
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


    public void showDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ShopActivityThree.this, R.style.CustomBottomSheetDialogTheme);
        dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(ShopActivityThree.this), R.layout.product_dialog, null, false);
        bottomSheetDialog.setContentView(dialogBinding.getRoot());

        getProducts();


        // Initialize Barcode Scanning Client


        positive_negative_discount = "1";
        positive_negative_normal = 1;

        dialogBinding.Product.setOnClickListener(v -> {
            dialog = new Dialog(ShopActivityThree.this, R.style.DialogTheme);
            if (!dialog.isShowing()) {

                dialog.setContentView(R.layout.dialog_spinner);

                dialog.show();

                TextInputLayout textInputLayout = dialog.findViewById(R.id.Search);
                ListView listView = dialog.findViewById(R.id.ItemList);

                TextView close = dialog.findViewById(R.id.Close);
                close.setOnClickListener(v1 -> dialog.dismiss());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ShopActivityThree.this, android.R.layout.simple_list_item_1, list);

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
                    dialogBinding.Product.setText(adapter.getItem(position));

                    int i = list.indexOf(adapter.getItem(position));

                    ProductModel model = arrayList.get(i);
                    if (model != null) {

                        if (model.getIsKitchen().equals("0")) {
                            product_id = model.getProduct_id();
                            name = model.getProduct_name();
                            price = model.getPrice();
                            product_code = model.getProduct_code();
                            is_kitchen = model.getIsKitchen();

                            portion1 = model.getPortion1();
                            portion1qty = model.getPortion1qty();
                            portion2 = model.getPortion2();
                            portion2qty = model.getPortion2qty();
                            portion3 = model.getPortion3();
                            portion3qty = model.getPortion3qty();
                            portion4 = model.getPortion4();
                            portion4qty = model.getPortion4qty();
                            portion5 = model.getPortion5();
                            portion5qty = model.getPortion5qty();


                        }

                        if (model.getIsKitchen().equals("1")) {
                            try {
                                int product_price = new Database(ShopActivityThree.this).fetch_outright_price(model.getPortion1()) +
                                        new Database(ShopActivityThree.this).fetch_outright_price(model.getPortion2()) +
                                        new Database(ShopActivityThree.this).fetch_outright_price(model.getPortion3()) +
                                        new Database(ShopActivityThree.this).fetch_outright_price(model.getPortion4()) +
                                        new Database(ShopActivityThree.this).fetch_outright_price(model.getPortion5());

                                product_id = model.getProduct_id();
                                name = model.getProduct_name();
                                price = String.valueOf(product_price);
                                product_code = model.getProduct_code();
                                is_kitchen = model.getIsKitchen();

                                portion1 = model.getPortion1();
                                portion1qty = model.getPortion1qty();
                                portion2 = model.getPortion2();
                                portion2qty = model.getPortion2qty();
                                portion3 = model.getPortion3();
                                portion3qty = model.getPortion3qty();
                                portion4 = model.getPortion4();
                                portion4qty = model.getPortion4qty();
                                portion5 = model.getPortion5();
                                portion5qty = model.getPortion5qty();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    dialog.dismiss();
                });
            }
        });

        dialogBinding.cancel.setOnClickListener(view -> bottomSheetDialog.dismiss());

        if (user.get(SessionManager.KEY_DISCOUNT_ENABLED).equals("Enabled")) {
            dialogBinding.DiscountLayout.setVisibility(View.VISIBLE);

            dialogBinding.ApplyDiscount.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    dialogBinding.InputLayout.setVisibility(View.VISIBLE);
                } else {
                    dialogBinding.Discount.getEditText().setText("");
                    dialogBinding.InputLayout.setVisibility(View.GONE);
                }
            });

            dialogBinding.PosNegButton.setOnClickListener(view -> {
                if (positive_negative_normal == 1){
                    dialogBinding.PosNegButton.setText("-");
                    positive_negative_normal++;
                    positive_negative_discount = "0";
                    dialogBinding.Discount.setStartIconDrawable(getResources().getDrawable(R.drawable.ic_baseline_remove_24));
                }else {
                    dialogBinding.PosNegButton.setText("+");
                    positive_negative_normal = 1;
                    positive_negative_discount = "1";
                    dialogBinding.Discount.setStartIconDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
               }
            });
        }

        dialogBinding.confirmButton.setOnClickListener(view -> {

            Location current = new Location(LocationManager.GPS_PROVIDER);
            current.setLatitude(latitude);
            current.setLongitude(longitude);

            Location customer = new Location(LocationManager.GPS_PROVIDER);
            customer.setLatitude(Double.parseDouble(sharedPreferences.getString("LAT", "")));
            customer.setLongitude(Double.parseDouble(sharedPreferences.getString("LNG", "")));

            if (validate()) {
                try {
                    if (Double.parseDouble(new DecimalFormat("0.00").format(current.distanceTo(customer) / 1000)) <= 500000) {
                        int discount = 0;
                    System.out.println("hchcjhc "+dialogBinding.Discount.getEditText().getText().toString().trim());
                        if (!dialogBinding.Discount.getEditText().getText().toString().trim().equals("")) {
                            discount = Integer.parseInt(dialogBinding.Discount.getEditText().getText().toString().trim());
                        }

                        int total_final_discount = discount * Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim());

                        if(positive_negative_discount.equals("0")){
                            final_price = Integer.parseInt(price) + discount;
                        }else{
                            final_price = Integer.parseInt(price) - discount;
                        }


                        sub_total = final_price * Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim());


                        if (is_kitchen.equals("0")) {
                            if (new Database(ShopActivityThree.this).fetch_product_quantity(product_id) >= Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim())) {
                                String response = new Database(getBaseContext()).create_sale(new SaleModel(
                                        product_id,
                                        product_code,
                                        name,
                                        String.valueOf(final_price),
                                        dialogBinding.Quantity.getEditText().getText().toString().trim(),
                                        String.valueOf(sub_total),
                                        sharedPreferences.getString("CUSTOMER_ID", ""),
                                        String.valueOf(total_final_discount)));

                                Toast.makeText(ShopActivityThree.this, response, Toast.LENGTH_LONG).show();
                                populateSales();
                            } else {
                                Toast.makeText(ShopActivityThree.this, "Quantity entered exceed the stock", Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (is_kitchen.equals("1")) {
                            if (new Database(ShopActivityThree.this).fetch_product_quantity(portion1) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion1qty)) &&
                                    new Database(ShopActivityThree.this).fetch_product_quantity(portion2) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion2qty)) &&
                                    new Database(ShopActivityThree.this).fetch_product_quantity(portion3) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion3qty)) &&
                                    new Database(ShopActivityThree.this).fetch_product_quantity(portion4) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion4qty)) &&
                                    new Database(ShopActivityThree.this).fetch_product_quantity(portion5) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion5qty))) {

                                String response = new Database(getBaseContext()).create_sale(new SaleModel(
                                        product_id,
                                        product_code,
                                        name,
                                        String.valueOf(final_price),
                                        dialogBinding.Quantity.getEditText().getText().toString().trim(),
                                        String.valueOf(sub_total),
                                        sharedPreferences.getString("CUSTOMER_ID", ""),
                                        String.valueOf(total_final_discount)));

                                Toast.makeText(ShopActivityThree.this, response, Toast.LENGTH_LONG).show();
                                populateSales();
                            } else {
                                Toast.makeText(ShopActivityThree.this, "Quantity entered exceed the stock", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else {
                        Toast.makeText(ShopActivityThree.this, "Please move closer to the customer shop", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                product_id = null;
                bottomSheetDialog.dismiss();
            }

        });
        bottomSheetDialog.show();


        dialogBinding.calculate.setOnClickListener(view -> {

            Location current = new Location(LocationManager.GPS_PROVIDER);
            current.setLatitude(latitude);
            current.setLongitude(longitude);

            Location customer = new Location(LocationManager.GPS_PROVIDER);
            customer.setLatitude(Double.parseDouble(sharedPreferences.getString("LAT", "")));
            customer.setLongitude(Double.parseDouble(sharedPreferences.getString("LNG", "")));

            if (validate()) {
                try {
                    if (Double.parseDouble(new DecimalFormat("0.00").format(current.distanceTo(customer) / 1000)) <= 500000) {
                        int discount = 0;
                        System.out.println("hchcjhc "+dialogBinding.Discount.getEditText().getText().toString().trim());
                        if (!dialogBinding.Discount.getEditText().getText().toString().trim().equals("")) {
                            discount = Integer.parseInt(dialogBinding.Discount.getEditText().getText().toString().trim());
                        }

                        int total_final_discount = discount * Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim());

                        if(positive_negative_discount.equals("0")){
                            final_price = Integer.parseInt(price) + discount;
                        }else{
                            final_price = Integer.parseInt(price) - discount;
                        }


                        sub_total = final_price * Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim());
                        dialogBinding.totalAmt.setText(String.valueOf(sub_total));


//                        if (is_kitchen.equals("0")) {
//                            if (new Database(ShopActivity.this).fetch_product_quantity(product_id) >= Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim())) {
//                                String response = new Database(getBaseContext()).create_sale(new SaleModel(
//                                        product_id,
//                                        product_code,
//                                        name,
//                                        String.valueOf(final_price),
//                                        dialogBinding.Quantity.getEditText().getText().toString().trim(),
//                                        String.valueOf(sub_total),
//                                        sharedPreferences.getString("CUSTOMER_ID", ""),
//                                        String.valueOf(total_final_discount)));
//
//                                Toast.makeText(ShopActivity.this, response, Toast.LENGTH_LONG).show();
//                                populateSales();
//                            } else {
//                                Toast.makeText(ShopActivity.this, "Quantity entered exceed the stock", Toast.LENGTH_SHORT).show();
//                            }
//                        }

//                        if (is_kitchen.equals("1")) {
//                            if (new Database(ShopActivity.this).fetch_product_quantity(portion1) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion1qty)) &&
//                                    new Database(ShopActivity.this).fetch_product_quantity(portion2) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion2qty)) &&
//                                    new Database(ShopActivity.this).fetch_product_quantity(portion3) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion3qty)) &&
//                                    new Database(ShopActivity.this).fetch_product_quantity(portion4) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion4qty)) &&
//                                    new Database(ShopActivity.this).fetch_product_quantity(portion5) >= (Integer.parseInt(dialogBinding.Quantity.getEditText().getText().toString().trim()) * Integer.parseInt(portion5qty))) {
//
//                                String response = new Database(getBaseContext()).create_sale(new SaleModel(
//                                        product_id,
//                                        product_code,
//                                        name,
//                                        String.valueOf(final_price),
//                                        dialogBinding.Quantity.getEditText().getText().toString().trim(),
//                                        String.valueOf(sub_total),
//                                        sharedPreferences.getString("CUSTOMER_ID", ""),
//                                        String.valueOf(total_final_discount)));
//
//                                Toast.makeText(ShopActivity.this, response, Toast.LENGTH_LONG).show();
////                                populateSales();
//                            } else {
//                                Toast.makeText(ShopActivity.this, "Quantity entered exceed the stock", Toast.LENGTH_SHORT).show();
//                            }
//                        }
                    }
                    else {
                        Toast.makeText(ShopActivityThree.this, "Please move closer to the customer shop", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                product_id = null;
//                bottomSheetDialog.dismiss();
            }

        });
        bottomSheetDialog.show();
    }

    public void showPayment(JSONArray array) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ShopActivityThree.this, R.style.CustomBottomSheetDialogTheme);
        paymentBinding = DataBindingUtil.inflate(LayoutInflater.from(ShopActivityThree.this), R.layout.payment_dialog, null, false);
        bottomSheetDialog.setContentView(paymentBinding.getRoot());

        paymentBinding.cancel.setOnClickListener(view -> bottomSheetDialog.dismiss());

        paymentBinding.TotalPrice.setText("Ksh " + total_payment);
        paymentBinding.GrandTotal.setText("Ksh " + total_payment);

        paymentBinding.RecyclerView.setHasFixedSize(true);
        paymentBinding.RecyclerView.setLayoutManager(new GridLayoutManager(ShopActivityThree.this, 3));

        try {
            paymentModel.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                PaymentModel model = new PaymentModel(
                        jsonObject.getString("id"),
                        jsonObject.getString("name")
                );
                paymentModel.add(model);
            }

            paymentAdapter = new PaymentAdapter(ShopActivityThree.this, paymentModel);
            paymentBinding.RecyclerView.setAdapter(paymentAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

        paymentBinding.Proceed.setOnClickListener(v -> {
            if (paymentModel.size() != 0) {
                List<PaymentModel> list = paymentAdapter.getSelectedItems();
                if (list.size() < 1 || list.size() > 2) {
                    Toast.makeText(ShopActivityThree.this, "Please select at most 2 payment methods", Toast.LENGTH_LONG).show();
                } else {
                    JSONArray jsonArray = new JSONArray();
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("items", new JSONArray(new Gson().toJson(new Database(ShopActivityThree.this).fetch_sales(sharedPreferences.getString("CUSTOMER_ID", "")))));
                        jsonArray.put(jsonObject);
                        System.out.println("hfhfhfhfhfhfhuyyyf" + jsonArray.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SharedPreferences preferences = getSharedPreferences("PAYMENT_SELECTED", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("PAYMENT_AMOUNT", String.valueOf(total_payment));
                    editor.putString("TOWN_ID", sharedPreferences.getString("TOWN_ID", ""));
                    editor.putString("PRODUCT_LIST", removeFirstandLast(jsonArray.toString()));
                    editor.putString("CUSTOMER_ID", sharedPreferences.getString("CUSTOMER_ID", ""));
                    editor.putString("SHOP_ID", sharedPreferences.getString("SHOP_ID", ""));
                    editor.putString("CUSTOMER_PHONE", sharedPreferences.getString("CUSTOMER_PHONE", ""));
                    editor.putString("TOTAL", String.valueOf(total_payment));
                    editor.putString("PAYMENT_METHOD", new Gson().toJson(list));
                    editor.putString("PRODUCT_LIST_RECEIPT", new Gson().toJson(new Database(ShopActivityThree.this).fetch_sales(sharedPreferences.getString("CUSTOMER_ID", ""))));
                    editor.putString("SELECTED_CUSTOMER_NAME", sharedPreferences.getString("SELECTED_CUSTOMER_NAME", ""));
                    editor.putString("SHOP_NAME", sharedPreferences.getString("CUSTOMER_NAME", ""));
                    editor.putString("DISCOUNT_ID", "");
                    editor.apply();

                    startActivity(new Intent(ShopActivityThree.this, PaymentActivity.class));

                    bottomSheetDialog.dismiss();
                }
            } else {
                Toast.makeText(ShopActivityThree.this, "Please select at most 2 payment methods", Toast.LENGTH_LONG).show();
            }
        });


        bottomSheetDialog.show();
    }

    public void submitDiscount() {
        JSONArray jsonArray = new JSONArray();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("items", new JSONArray(new Gson().toJson(new Database(ShopActivityThree.this).fetch_sales(sharedPreferences.getString("CUSTOMER_ID", "")))));
            jsonArray.put(jsonObject);
            System.out.println("hfhfhfhfhfhfhuyyyf" + jsonArray.toString());


            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("discount", "1")
                    .addFormDataPart("discount_id", "")
                    .addFormDataPart("invoice", "0")
                    .addFormDataPart("invoice_id", "")
                    .addFormDataPart("cheque", "0")
                    .addFormDataPart("cheque_id", "")
                    .addFormDataPart("json", removeFirstandLast(jsonArray.toString()))
                    .addFormDataPart("customer_id", sharedPreferences.getString("CUSTOMER_ID", ""))
                    .addFormDataPart("distributor_id", user.get(SessionManager.KEY_DISTRIBUTOR_ID))
                    .addFormDataPart("town_id", sharedPreferences.getString("TOWN_ID", ""))
                    .addFormDataPart("salesman_id", user.get(SessionManager.KEY_SALESMAN_ID))
                    .addFormDataPart("paid_by", "")
                    .addFormDataPart("vehicle_id", user.get(SessionManager.KEY_VEHICLE_ID))
                    .addFormDataPart("payment_status", "")
                    .addFormDataPart("shop_id", sharedPreferences.getString("SHOP_ID", ""))
                    .addFormDataPart("total", String.valueOf(total_payment))
                    .addFormDataPart("payments", "")
                    .addFormDataPart("signature", "")
                    .build();

            Request request = new Request.Builder()
                    .url(BaseURL.ROUTE_URL_NEW + "addSale")
//                    .url(BaseURL.ROUTE_URL_NEW + "index.php/api/addSale")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        sweetAlertDialog.dismiss();
                        Toast.makeText(ShopActivityThree.this, "No Connection to host", Toast.LENGTH_SHORT).show();
                        System.out.println("errorrr" + e.toString());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            System.out.println("jsonObjectiiiii" + jsonObject);
                                if (jsonObject.getString("success").equals("1")) {
                                new Database(ShopActivityThree.this).clear_customer_sales(sharedPreferences.getString("CUSTOMER_ID", ""));
                                sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                sweetAlertDialog.setTitle("Discount sale added successfully. Please contact system admin for approval.");
                                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
                                    sweetAlertDialog1.dismiss();
                                    Intent intent = new Intent(ShopActivityThree.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                });

                            } else {
                                sweetAlertDialog.dismiss();
                                Toast.makeText(ShopActivityThree.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {
                            sweetAlertDialog.dismiss();
                            Log.d("Error on payment2:",e.toString());
                            Toast.makeText(ShopActivityThree.this, "An error occurred", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }


                    });

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validate() {
        boolean valid = true;

        if (product_id == null) {
            Toast.makeText(ShopActivityThree.this, "Please select product", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (dialogBinding.Quantity.getEditText().getText().toString().trim().equals("")) {
            dialogBinding.Quantity.setError("Please input product quantity");
            valid = false;
        } else {
            dialogBinding.Quantity.setError("");
        }

        if (dialogBinding.ApplyDiscount.isChecked()) {
            if (dialogBinding.Discount.getEditText().getText().toString().trim().equals("")) {
                dialogBinding.Discount.setError("Please input discount");
                valid = false;
            } else {
                dialogBinding.Discount.setError("");
            }
        }

        return valid;
    }

    public void attendDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ShopActivityThree.this, R.style.CustomBottomSheetDialogTheme);
        attendBinding = DataBindingUtil.inflate(LayoutInflater.from(ShopActivityThree.this), R.layout.attend_dialog, null, false);
        bottomSheetDialog.setContentView(attendBinding.getRoot());

        attendBinding.cancel.setOnClickListener(view -> bottomSheetDialog.dismiss());
        attendBinding.AttendName.setText("Enter reason for not attending to " + sharedPreferences.getString("SELECTED_CUSTOMER_NAME", ""));

        attendBinding.confirmButton.setOnClickListener(v -> {
            if (attendBinding.Reason.getEditText().getText().toString().trim().equals("")) {
                attendBinding.Reason.setError("Please input reason");
            } else {
                bottomSheetDialog.dismiss();
                fillTicket();
            }
        });


        bottomSheetDialog.show();
    }

    public void fillTicket() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ShopActivityThree.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitle("Please Wait...");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("customer_id", sharedPreferences.getString("CUSTOMER_ID", ""))
                .addFormDataPart("salesman_id", user.get(SessionManager.KEY_SALESMAN_ID))
                .addFormDataPart("reason", attendBinding.Reason.getEditText().getText().toString().trim())
                .addFormDataPart("shop_id", sharedPreferences.getString("SHOP_ID", ""))
                .addFormDataPart("distributor_id", user.get(SessionManager.KEY_DISTRIBUTOR_ID))
                .addFormDataPart("vehicle_id", user.get(SessionManager.KEY_VEHICLE_ID))
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "fillTicket")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    sweetAlertDialog.dismiss();
                    Toast.makeText(ShopActivityThree.this, "No Connection to host", Toast.LENGTH_SHORT).show();
                    System.out.println("errorrr" + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        System.out.println("jsonObjectiiiii" + jsonObject);
                        if (jsonObject.getString("success").equals("1")) {
                            sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            sweetAlertDialog.setTitle("Ticket submitted successfully.");
                            sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
                                sweetAlertDialog1.dismiss();
                                onBackPressed();
                            });

                        } else {
                            sweetAlertDialog.dismiss();
                            Toast.makeText(ShopActivityThree.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        sweetAlertDialog.dismiss();
                        Log.d("Error on payment3:",e.toString());
                        Toast.makeText(ShopActivityThree.this, "An error occurred", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }


    public void populateSales() {
        saleModel.clear();
        saleModel = (ArrayList<SaleModel>) new Database(getBaseContext()).fetch_sales(sharedPreferences.getString("CUSTOMER_ID", ""));

        saleAdapter = new SaleAdapter(ShopActivityThree.this, saleModel, this);
        saleAdapter.notifyDataSetChanged();
        binding.RecyclerView.setAdapter(saleAdapter);

        total_payment = 0;
        total_discount = 0;

        for (SaleModel model : saleModel) {
            total_payment += Integer.parseInt(model.getTotal());
            total_discount += Integer.parseInt(model.getDiscount());
        }

        binding.TotalDue.setText("Kshs " + total_payment);
        binding.TotalAmount.setText("Kshs " + total_payment);


        if (saleModel.size() == 0) {
            binding.Head.setVisibility(View.GONE);
            binding.Lay.setVisibility(View.VISIBLE);
            binding.StatusCard.setVisibility(View.GONE);
            binding.TrolleyItems.setVisibility(View.GONE);

            binding.NotAttending.setVisibility(View.VISIBLE);
            binding.NotAttending.setText("Not attending to " + sharedPreferences.getString("SELECTED_CUSTOMER_NAME", "") + "?");
        } else {
            binding.Head.setVisibility(View.VISIBLE);
            binding.Lay.setVisibility(View.GONE);
            binding.StatusCard.setVisibility(View.VISIBLE);
            binding.TrolleyItems.setVisibility(View.VISIBLE);

            binding.NotAttending.setChecked(false);
            binding.NotAttending.setVisibility(View.GONE);
        }


    }


    public void populateProducts(List<ProductModel> products) {
        // Clear existing data
        list.clear();
        arrayList.clear();

        // Log the products list
        Log.d("populateProducts", "Logging the list of products:");

        for (ProductModel product : products) {
            // Log the details of each product
            Log.d("populateProducts", "Product ID: " + product.getProduct_id());
            Log.d("populateProducts", "Product Name: " + product.getProduct_name());
            Log.d("populateProducts", "Product Code: " + product.getProduct_code());
            Log.d("populateProducts", "Product Price: " + product.getPrice());
//            Log.d("populateProducts", "Product Stock: " + product.getStock());
//            Log.d("populateProducts", "Product Category: " + product.getCategory());
//            Log.d("populateProducts", "Product Category ID: " + product.getCategory_id());
//            Log.d("populateProducts", "Product Type: " + product.getType());
//            Log.d("populateProducts", "Product Image: " + product.getImage());
//            Log.d("populateProducts", "Product Sales: " + product.getSales());
            Log.d("populateProducts", "-----------");

            // Add the product names to the list and the product models to the arrayList
            list.add(product.getProduct_name());
            arrayList.add(product);
        }

        // Notify your adapter (if you have one) about the data change
        // Example: yourAdapter.notifyDataSetChanged();
    }

    @Override
    public void shopData() {
        populateSales();
    }
}