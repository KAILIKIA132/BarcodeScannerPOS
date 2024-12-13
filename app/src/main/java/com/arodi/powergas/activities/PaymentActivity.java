package com.arodi.powergas.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.DetailAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityPaymentBinding;
import com.arodi.powergas.models.CityModel;
import com.arodi.powergas.models.PaymentModel;
import com.arodi.powergas.models.ProductCodeModel;
import com.arodi.powergas.models.ProductHistory;
import com.arodi.powergas.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentActivity extends AppCompatActivity {
    ActivityPaymentBinding binding;
    SharedPreferences preferences;
    SweetAlertDialog dialog;
    HashMap<String, String> user;
    HashMap<String, String> hash_map = new HashMap<>();
    ProductCodeModel product;
    String BILL = "";
    public String isInvoice = "0";
    public String isCheque = "0";

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    public BluetoothSocket mBluetoothSocket;
    public ProgressDialog progressDialog;

    String StringPaymentArray;
    String StringProductArray;

    String fmt5 = "%6s %6s %6s %6s";
    String fmt2 = "%6s %6s %6s\n";

    Bitmap bitmap;
    String grandTotals;
    public UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment);

        grandTotals = String.valueOf(getIntent().getDoubleExtra("grand_totals", 0.0));
Log.d("Grand_Totals",grandTotals);
        // Retrieve the ProductCodeModel object
        product = getIntent().getParcelableExtra("product_data");
        if (product != null) {
            // Check if price is empty and set default value if needed
            if (TextUtils.isEmpty(product.getPrice())) {
                product.setPrice("0"); // Set default value if price is empty
            }

            // Now set the price

            String price = product.getPrice();
            Log.d("PaymentActivity1", "Product ID: " + product.getId());
            Log.d("PaymentActivity1", "Product Name: " + product.getName());
            Log.d("PaymentActivity1", "Product Code: " + product.getCode());
            Log.d("PaymentActivity1", "Product Price: " + product.getPrice());

            // Set the price in the TotalAmountPaid TextView
            binding.TotalAmountPaid.setText(price);
//            binding.TotalAmountPaid.invalidate(grandTotals);  // Force UI update if needed
            // Set the price in the TotalAmountPaid TextView
            binding.TotalAmountPaid.setText(grandTotals);
        }
        else {
            Toast.makeText(this, "No product data received", Toast.LENGTH_SHORT).show();
        }



        user = new SessionManager(PaymentActivity.this).getLoginDetails();

        preferences = getSharedPreferences("PAYMENT_SELECTED", Context.MODE_PRIVATE);
        if (preferences.contains("PAYMENT_AMOUNT")) {

            binding.TotalAmountPaid.setText(preferences.getString("PAYMENT_AMOUNT", ""));

            try {
                JSONArray jsonArray = new JSONArray(preferences.getString("PAYMENT_METHOD", ""));
                System.out.println("jsjdjdjdk" + jsonArray);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if (jsonObject.getString("name").equals("Mpesa Payment")) {
                        binding.MpesaCard.setVisibility(View.VISIBLE);

                        binding.Amount.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                binding.TotalAmountPaid.setText(String.valueOf(calculatePrice()));
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    }

                    if (jsonObject.getString("name").equals("Cash Payment")) {
                        binding.CashCard.setVisibility(View.VISIBLE);

                        binding.CashAmount.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                binding.TotalAmountPaid.setText(String.valueOf(calculatePrice()));
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    }

                    if (jsonObject.getString("name").equals("Cheque Payment")) {
                        binding.ChequeCard.setVisibility(View.VISIBLE);

                        binding.ChequeAmount.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                binding.TotalAmountPaid.setText(String.valueOf(calculatePrice()));
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });

                    }

                    if (jsonObject.getString("name").equals("Invoice Payment")) {
                        binding.InvoiceCard.setVisibility(View.VISIBLE);
                        binding.InvoiceAmount.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                binding.TotalAmountPaid.setText(String.valueOf(calculatePrice()));
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        populateSales();

        binding.CaptureImage.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(PaymentActivity.this.getPackageManager()) != null) {
                startActivityForResult(intent, 480);
            }
        });

        binding.Clear.setOnClickListener(v -> binding.signaturePad.clear());

        if (binding.MpesaCard.getVisibility() == View.VISIBLE) {
            hash_map.put("Mpesa", "Mpesa Payment");
        }

        if (binding.CashCard.getVisibility() == View.VISIBLE) {
            hash_map.put("Cash", "Cash Payment");
        }

        if (binding.ChequeCard.getVisibility() == View.VISIBLE) {
            hash_map.put("Cheque", "Cheque Payment");
        }

        if (binding.InvoiceCard.getVisibility() == View.VISIBLE) {
            hash_map.put("Invoice", "Invoice Payment");
        }

        //only cheque
        if (!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && !checkInvoice(hash_map)) {
            sendSmsToCustomer();
            binding.DeptLayout.setVisibility(View.VISIBLE);
        }
        //only invoice
        if (!checkCash(hash_map) && !checkMpesa(hash_map) && !checkCheque(hash_map) && checkInvoice(hash_map)) {
            sendSmsToCustomer();
            binding.DeptLayout.setVisibility(View.VISIBLE);
        }
        //only cheque and invoice
        if (!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && checkInvoice(hash_map)) {
            sendSmsToCustomer();
            binding.DeptLayout.setVisibility(View.VISIBLE);
        }

        //if either cash or mpesa and cheque or invoice output partial
        if ((checkCash(hash_map) || checkMpesa(hash_map)) && (checkCheque(hash_map) || checkInvoice(hash_map))) {
            sendSmsToCustomer();
            binding.DeptLayout.setVisibility(View.VISIBLE);
        }

        binding.back.setOnClickListener(v -> onBackPressed());

        binding.Lipa.setOnClickListener(v -> {
            if (validate()) {
                dialog = new SweetAlertDialog(PaymentActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                dialog.setCancelable(false);
                dialog.setTitle("Please Wait...");
                dialog.show();

                if (binding.MpesaCard.getVisibility() == View.VISIBLE) {
                    sendStkPush();
                } else {

//                    Toast.makeText(PaymentActivity.this, "No Payment Type1", Toast.LENGTH_SHORT).show();

                    checkPaymentType();
                }

            }
        });

    }


    public void sendStkPush() {
        String submitted_phone = "254" + (binding.PhoneNumber.getEditText().getText().toString().trim().length() >= 9 ? binding.PhoneNumber.getEditText().getText().toString().trim().substring(binding.PhoneNumber.getEditText().getText().toString().trim().length() - 9) : "");

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(BaseURL.SERVER_URL + "trans/stkpush.php?PhoneNumber=" + submitted_phone + "&Amount=" + binding.Amount.getEditText().getText().toString().trim())
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(PaymentActivity.this, "No Connection to host", Toast.LENGTH_SHORT).show();
                    System.out.println("errorrr" + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        System.out.println("jsonObpasswordjectiiiii" + object);

                        if (object.getString("ResponseCode").equals("0")) {
                            String CheckoutRequestID = object.getString("CheckoutRequestID");
                            new Handler().postDelayed(() -> confirmPayment(CheckoutRequestID), 20000);

                        } else {
                            dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            dialog.setTitle(object.getString("ResponseDescription"));
                            dialog.setConfirmClickListener(Dialog::dismiss);
                        }

                    } catch (Exception e) {
                        System.out.println("nhdhdhdhdd" + e);
                        dialog.dismiss();
                        Toast.makeText(PaymentActivity.this, "An error occurred, Kindly check your inputs.", Toast.LENGTH_SHORT).show();
                    }


                });

            }
        });

    }


    public void confirmPayment(String CheckoutRequestID) {
        dialog.setTitle("Confirming Payment");

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "trans/confirm_url.php?checkoutRequestID=" + CheckoutRequestID)
                .method("GET", null)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(PaymentActivity.this, "No Connection to host", Toast.LENGTH_SHORT).show();
                    System.out.println("errorrr" + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        System.out.println("jsonObpasswordjectiiiii" + object);

                        if (object.getString("success").equals("1")){
                            JSONObject jsonObject = new JSONObject(object.getString("response"));

                            if (jsonObject.has("resultCode")) {
                                if (jsonObject.getString("resultCode").equals("0")) {
                                    checkPaymentType();
                                    Toast.makeText(PaymentActivity.this, "No Payment Type2", Toast.LENGTH_SHORT).show();

                                } else {
                                    dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                    dialog.setTitle(jsonObject.getString("resultDesc"));
                                    dialog.setConfirmClickListener(Dialog::dismiss);
                                }
                            }else {
                                dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                dialog.setTitle("An error occurred... Please try again");
                                dialog.setConfirmClickListener(Dialog::dismiss);
                            }

                        }else if (object.getString("success").equals("0")){
                            new Handler().postDelayed(() -> confirmPayment(CheckoutRequestID), 20000);
                        }else {
                            dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            dialog.setTitle(object.getString("message"));
                            dialog.setConfirmClickListener(Dialog::dismiss);
                        }


                    } catch (Exception e) {
                        dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        dialog.setTitle("An error occurred... Please try again later");
                        dialog.setConfirmClickListener(Dialog::dismiss);

                        System.out.println("hgfgdgdhhdhd" + e);
                    }
                });
            }
        });

    }

    public void checkPaymentType() {
        submitSale();


        //only cash
//        if (checkCash(hash_map) && !checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
//            submitSale();
//        }
//        //only mpesa
//        if (!checkCash(hash_map) && checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
//            submitSale();
//        }
//        //only cheque
//        if (!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && !checkInvoice(hash_map)) {
//            confirmCode();
//        }
//        //only invoice
//        if (!checkCash(hash_map) && !checkMpesa(hash_map) && !checkCheque(hash_map) && checkInvoice(hash_map)) {
//            confirmCode();
//        }
//
//        //only cash and mpesa
//        if (checkCash(hash_map) && checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
//            submitSale();
//        }
//
//        //only cheque and invoice
//        if (!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && checkInvoice(hash_map)) {
//            confirmCode();
//        }
//
//        //if either cash or mpesa and cheque or invoice output partial
//        if ((checkCash(hash_map) || checkMpesa(hash_map)) && (checkCheque(hash_map) || checkInvoice(hash_map))) {
//            confirmCode();
//        }

    }

    public void sendSmsToCustomer() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "sms/send_sms.php?customer_phone=" + preferences.getString("CUSTOMER_PHONE", "") + "&user_id=" + user.get(SessionManager.KEY_USER_ID) + "&customer_id=" + preferences.getString("CUSTOMER_ID", ""))
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
                        System.out.println("jjfjfhfjdfjfjf" + response.body().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

            }
        });
    }

    public static String removeFirstandLast(String str) {
        str = str.substring(1, str.length() - 1);
        return str;
    }

    public void confirmCode() {
        String cheque_image = null;
        if (bitmap != null){
            cheque_image = getStringImage(bitmap);
        }

        JSONArray array1 = new JSONArray();
        JSONObject object1 = new JSONObject();
        try {
            object1.put("verification_code", binding.Code.getEditText().getText().toString().trim());
            object1.put("signature", getStringImage(binding.signaturePad.getSignatureBitmap()));
            object1.put("user_id", user.get(SessionManager.KEY_USER_ID));
            object1.put("cheque_image", cheque_image);
            object1.put("customer_id", preferences.getString("CUSTOMER_ID", ""));

            array1.put(object1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, array1.toString());

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "sms/confirm_sms.php?action=confirm_code")
                .method("POST", body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(PaymentActivity.this, "No connection to host", Toast.LENGTH_SHORT).show();
                    System.out.println("errorrr" + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        System.out.println("jjfjfhfjdfjfjf" + object);
                        if (object.getString("success").equals("1")) {
//                            Toast.makeText(PaymentActivity.this, "No Payment Type", Toast.LENGTH_SHORT).show();

                            submitSale();
                        } else {
                            Toast.makeText(PaymentActivity.this, object.getString("message"), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    } catch (Exception e) {
                        dialog.dismiss();
                        Toast.makeText(PaymentActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                });

            }
        });


    }




        // OkHttpClient instance

        // Function to send the sale data via POST request

    public void submitSale() {
        String API_URL = "https://techsavanna.technology/summit-pos/mobileApi/api/productsApi.php";

        try {
            // Get today's date in the format yyyy-MM-dd
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = sdf.format(new Date());
            Log.d("product",product.getName());
            Log.d("product",product.getCode());
            Log.d("product",product.getPrice());
            Log.d("product",product.getId());
            Log.d("product",currentDate);

            // Build the JSON object
            JSONObject sale = new JSONObject();
            sale.put("date", currentDate);
            sale.put("reference_no", "REF123456");//what is reference_no
            sale.put("customer_id", 1);
            sale.put("customer", "John Doe");
            sale.put("biller_id", 1);
            sale.put("biller", "Store Name");
            sale.put("chef_id", 2);
            sale.put("chef", "Chef Name");
            sale.put("cashier_id", 3);
            sale.put("cashier", "Cashier Name");
            sale.put("warehouse_id", 1);
            sale.put("note", "Customer requested gift wrapping.");
            sale.put("staff_note", "Handle with care.");
            sale.put("total", grandTotals);
            sale.put("product_discount", 10.00);
            sale.put("order_discount_id", JSONObject.NULL);
            sale.put("order_discount", 5.00);
            sale.put("total_discount", 15.00);
            sale.put("product_tax", 16.00);
            sale.put("order_tax_id", JSONObject.NULL);
            sale.put("order_tax", 2.00);
            sale.put("total_tax", 18.00);
            sale.put("shipping", 5.00);
            sale.put("grand_total", 115.00);
            sale.put("total_items", 3);
            sale.put("sale_status", "completed");
            sale.put("payment_status", "paid");
            sale.put("payment_term", "Immediate");

            // Products array
            JSONArray products = new JSONArray();
            JSONObject product1 = new JSONObject();
            product1.put("product_id", product.getId());
            product1.put("product_code", product.getCode());
            product1.put("product_name", product.getName());
            product1.put("product_type", product.getType());
            product1.put("option_id", JSONObject.NULL);
            product1.put("net_unit_price", product.getPrice());
            product1.put("unit_price", product.getPrice());
            product1.put("quantity", 1);
            product1.put("warehouse_id", 1);
            product1.put("item_tax", 8.00);
            product1.put("tax_rate_id", 1);
            product1.put("tax", 2.00);
            product1.put("discount", 5.00);
            product1.put("item_discount", 3.00);
            product1.put("subtotal", grandTotals);
            product1.put("serial_no", "SN123456");
            product1.put("real_unit_price", 30.00);
            products.put(product1);

            sale.put("products", products);

            // Payment array
            JSONArray payments = new JSONArray();
            JSONObject payment1 = new JSONObject();
            payment1.put("date", currentDate);
            payment1.put("reference_no", "PAY123456");
            payment1.put("amount", product.getPrice());
            payment1.put("paid_by", "cash");
            payment1.put("cheque_no", JSONObject.NULL);
            payment1.put("cc_no", "4111111111111111");
            payment1.put("cc_holder", "John Doe");
            payment1.put("cc_month", "12");
            payment1.put("cc_year", "2025");
            payment1.put("cc_type", "Visa");
            payment1.put("cc_cvv2", "123");
            payment1.put("created_by", "admin");
            payment1.put("type", "sale");
            payment1.put("note", "Paid in full");
            payment1.put("pos_paid", 0.00);
            payment1.put("pos_balance", 0.00);
            payments.put(payment1);

            sale.put("payment", payments);

            // Log the payload before sending it
            Log.d("SubmitSalePayload", sale.toString());

            // Send the POST request
            post(API_URL, sale.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


        // Helper function to send the POST request
        private void post(String url, String json) {

            OkHttpClient client = new OkHttpClient();

            // MediaType for JSON data
           final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(JSON, json);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace(); // Handle request failure
                    Log.d("Exception",e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        dialog.dismiss();

                        // Show Toast on the main thread
                        runOnUiThread(() -> {
                            Toast.makeText(PaymentActivity.this, "SALE ADDED SUCCESSFULLY", Toast.LENGTH_LONG).show();


                            Intent intent = new Intent(PaymentActivity.this, ReceiptActivity.class);
                            intent.putExtra("product_data", product);  // Ensure 'product' implements Serializable/Parcelable
                            intent.putExtra("grand_totals", grandTotals);  // Pass the grand_totals value
                            startActivity(intent);
                            dialog.dismiss(); // Dismiss dialog safely


                        });

                        System.out.println("Response: " + responseBody);
                        Log.d("response", responseBody);
                    } else {
                        dialog.dismiss();

                        // You might want to handle error responses similarly
                        // Show Toast on the main thread for failure case too
                        runOnUiThread(() -> {
                            Toast.makeText(PaymentActivity.this, "Request failed: " + response.code(), Toast.LENGTH_LONG).show();
                        });

                        System.out.println("Request failed: " + response.code());
                        Log.d("response failed", String.valueOf(response.code()));
                    }
                }
            });
        }



//    public void submitSale(String signature, String cheque_image) {
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .build();
//
//        // Construct the sale JSON object
//        JSONObject saleJson = new JSONObject();
//        try {
//            saleJson.put("date", "2024-09-30");
//            saleJson.put("reference_no", "REF123456");
//            saleJson.put("customer_id", 1);
//            saleJson.put("customer", "John Doe");
//            saleJson.put("biller_id", 1);
//            saleJson.put("biller", "Store Name");
//            saleJson.put("chef_id", 2);
//            saleJson.put("chef", "Chef Name");
//            saleJson.put("cashier_id", 3);
//            saleJson.put("cashier", "Cashier Name");
//            saleJson.put("warehouse_id", 1);
//            saleJson.put("note", "Customer requested gift wrapping.");
//            saleJson.put("staff_note", "Handle with care.");
//            saleJson.put("total", 100.00);
//            saleJson.put("product_discount", 10.00);
//            saleJson.put("order_discount_id", JSONObject.NULL);
//            saleJson.put("order_discount", 5.00);
//            saleJson.put("total_discount", 15.00);
//            saleJson.put("product_tax", 16.00);
//            saleJson.put("order_tax_id", JSONObject.NULL);
//            saleJson.put("order_tax", 2.00);
//            saleJson.put("total_tax", 18.00);
//            saleJson.put("shipping", 5.00);
//            saleJson.put("grand_total", 115.00);
//            saleJson.put("total_items", 3);
//            saleJson.put("sale_status", "completed");
//            saleJson.put("payment_status", "paid");
//            saleJson.put("payment_term", "Immediate");
//
//            // Construct the products array
//            JSONArray productsArray = new JSONArray();
//            JSONObject product = new JSONObject();
//            product.put("product_id", 1);
//            product.put("product_code", "PROD001");
//            product.put("product_name", "Product Name 1");
//            product.put("product_type", "type1");
//            product.put("option_id", JSONObject.NULL);
//            product.put("net_unit_price", 30.00);
//            product.put("unit_price", 35.00);
//            product.put("quantity", 2);
//            product.put("warehouse_id", 1);
//            product.put("item_tax", 8.00);
//            product.put("tax_rate_id", 1);
//            product.put("tax", 2.00);
//            product.put("discount", 5.00);
//            product.put("item_discount", 3.00);
//            product.put("subtotal", 60.00);
//            product.put("serial_no", "SN123456");
//            product.put("real_unit_price", 30.00);
//            productsArray.put(product);
//
//            // Add products array to sale JSON
//            saleJson.put("products", productsArray);
//
//            // Construct the payment array
//            JSONArray paymentsArray = new JSONArray();
//            JSONObject payment = new JSONObject();
//            payment.put("date", "2024-09-30");
//            payment.put("reference_no", "PAY123456");
//            payment.put("amount", 115.00);
//            payment.put("paid_by", "credit_card");
//            payment.put("cheque_no", JSONObject.NULL);
//            payment.put("cc_no", "4111111111111111");
//            payment.put("cc_holder", "John Doe");
//            payment.put("cc_month", "12");
//            payment.put("cc_year", "2025");
//            payment.put("cc_type", "Visa");
//            payment.put("cc_cvv2", "123");
//            payment.put("created_by", "admin");
//            payment.put("type", "sale");
//            payment.put("note", "Paid in full");
//            payment.put("pos_paid", 115.00);
//            payment.put("pos_balance", 0.00);
//            paymentsArray.put(payment);
//
//            // Add payment array to sale JSON
//            saleJson.put("payment", paymentsArray);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("json", saleJson.toString())  // Add the JSON payload
//                .addFormDataPart("image", cheque_image)
//                .addFormDataPart("signature", signature)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(BaseURL.ROUTE_URL + "productsApi.php")
//                .post(requestBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(() -> {
//                    dialog.dismiss();
//                    Toast.makeText(PaymentActivity.this, "No Connection to host", Toast.LENGTH_SHORT).show();
//                    System.out.println("error: " + e.toString());
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//                runOnUiThread(() -> {
//                    try {
//                        JSONObject responseJson = new JSONObject(response.body().string());
//                        if (responseJson.getString("success").equals("1")) {
//                            dialog.dismiss();
//                            // Handle sale added successfully
//                            Toast.makeText(PaymentActivity.this, "SALE ADDED SUCCESSFULLY", Toast.LENGTH_LONG).show();
//                        } else {
//                            dialog.dismiss();
//                            Toast.makeText(PaymentActivity.this, responseJson.getString("message"), Toast.LENGTH_LONG).show();
//
//                        }
//                    } catch (Exception e) {
//                        dialog.dismiss();
//                        Toast.makeText(PaymentActivity.this, "An error occurred", Toast.LENGTH_LONG).show();
//                        e.printStackTrace();
//                    }
//                });
//            }
//        });
//    }

    public void goBackToSales() {
        SharedPreferences sharedPreferences = getSharedPreferences("CUSTOMER_SHOPPING_DATA", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("SAVED_INSTANCE")) {

            if (sharedPreferences.getString("SAVED_INSTANCE", "").equals("HISTORY_LIST")) {
                Intent intent = new Intent(PaymentActivity.this, SaleHistory.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            if (sharedPreferences.getString("SAVED_INSTANCE", "").equals("CUSTOMER_LIST")) {
                Intent intent = new Intent(PaymentActivity.this, MakeSaleActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            if (sharedPreferences.getString("SAVED_INSTANCE", "").equals("DISCOUNT_LIST")) {
                Intent intent = new Intent(PaymentActivity.this, DiscountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            onBackPressed();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            progressDialog.dismiss();
            printReceipt();
        }
    };

    public void ListPairedDevices() {
        Set<BluetoothDevice> bondedDevices = this.mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice next : bondedDevices) {
                System.out.println("PairedDevices: " + next.getName() + "  " + next.getAddress());
            }
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);

        if (i == 480 && i2 == RESULT_OK && intent != null && intent.getExtras() != null) {
            bitmap = (Bitmap) intent.getExtras().get("data");
            binding.ImagePreview.setVisibility(View.VISIBLE);
            binding.ImagePreviewText.setVisibility(View.GONE);

            binding.ImagePreview.setImageBitmap(bitmap);
        }

        if (i != 1) {
            if (i == 2) {
                if (i2 == -1) {
                    ListPairedDevices();
                    startActivityForResult(new Intent(this, DeviceListActivity.class), 1);
                    return;
                }
            }
        } else if (i2 == -1) {
            String string = intent.getExtras().getString("DeviceAddress");
            System.out.println("Coming incoming address " + string);
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(string);
            progressDialog = ProgressDialog.show(PaymentActivity.this, "Connecting...", mBluetoothDevice.getName() + " : " + mBluetoothDevice.getAddress(), true, true);
            new Thread(() -> {
                try {
                    this.mBluetoothSocket = this.mBluetoothDevice.createRfcommSocketToServiceRecord(this.applicationUUID);
                    this.mBluetoothAdapter.cancelDiscovery();
                    this.mBluetoothSocket.connect();
                    this.mHandler.sendEmptyMessage(0);
                } catch (IOException e) {
                    System.out.println("CouldNotConnectToSocket" + e);
                    this.progressDialog.dismiss();
                    closeSocket(this.mBluetoothSocket);
                    goBackToSales();
                }
            }).start();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            if (this.mBluetoothSocket != null) {
                this.mBluetoothSocket.close();
            }
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try {
            if (this.mBluetoothSocket != null) {
                this.mBluetoothSocket.close();
            }
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
        setResult(0);
    }

    private void closeSocket(BluetoothSocket bluetoothSocket) {
        try {
            bluetoothSocket.close();
            System.out.println("SocketClosed");
        } catch (IOException unused) {
            System.out.println("CouldNotCloseSocket");
        }
    }


    public void populateSales() {
        try {
            StringBuilder sb = new StringBuilder();
            JSONArray jsonArray = new JSONArray(preferences.getString("PRODUCT_LIST_RECEIPT", ""));
            System.out.println("fhfhfhfhf" + jsonArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                sb.append(String.format(fmt5, new Object[]{jsonObject.getString("name"), jsonObject.getString("quantity"), jsonObject.getString("price"), jsonObject.getString("total") + "\n"}));
            }
            StringProductArray = String.valueOf(sb);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void printReceipt() {
        new Thread() {
            public void run() {
                try {
                    OutputStream outputStream = mBluetoothSocket.getOutputStream();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    BILL = "RECEIPT \nPOWER GAS \nTEL NO: 0748911778 \nVAN :" + user.get(SessionManager.KEY_PLATE_NO)
                            + "\nRECEIPT NO: " + "1" + "\nDATE : " + simpleDateFormat.format(new Date()) + "\n";

                    BILL = BILL + "================================\n CUSTOMER NAME: " + preferences.getString("SELECTED_CUSTOMER_NAME", "") + "\nSHOP NAME: " + preferences.contains("SHOP_NAME") + " \n\n";
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(BILL);

                    sb2.append(String.format(fmt5, new Object[]{"Item", "Qty", "Price", "Total"}));
                    BILL = sb2.toString();
                    BILL = BILL + "\n--------------------------------\n";
                    BILL = BILL + StringProductArray;
                    BILL = BILL + "\nTOTAL : " + preferences.getString("TOTAL", "") + "\n";
                    BILL = BILL + "================================\n";
                    BILL = BILL + "\nPayments : \n";
                    BILL = BILL + String.format(fmt2, new Object[]{"Method", "Amount", "Status"});
                    BILL = BILL + "\n--------------------------------\n";
                    BILL = BILL + StringPaymentArray;
                    BILL = BILL + "\n--------------------------------\n";
                    BILL = BILL + "Payment Status " + "paid" + "\n";
                    BILL = BILL + "\n--------------------------------";
                    BILL = BILL + "\nServed By :" + user.get(SessionManager.KEY_USERNAME);
                    BILL = BILL + "\nON : " + simpleDateFormat.format(new Date());
                    BILL = BILL + "\n" + "POWER GAS \nRAFIKI JIKONI \n";
                    BILL = BILL + "--------------------------------\n";
                    BILL = BILL + "\n\n";

                    outputStream.write(BILL.getBytes(), 0, BILL.getBytes().length);
                    outputStream.write(BILL.getBytes(), 0, BILL.getBytes().length);

                    runOnUiThread(()->goBackToSales());
                } catch (Exception e) {
                    Log.e("MainActivity", "Exe ", e);
                }
            }
        }.start();
    }

    public String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByteArray, Base64.DEFAULT);
    }

    public boolean checkCash(HashMap<String, String> hash_map) {
        return hash_map.containsValue("Cash Payment");
    }

    public boolean checkMpesa(HashMap<String, String> hash_map) {
        return hash_map.containsValue("Mpesa Payment");
    }

    public boolean checkCheque(HashMap<String, String> hash_map) {
        return hash_map.containsValue("Cheque Payment");
    }

    public boolean checkInvoice(HashMap<String, String> hash_map) {
        return hash_map.containsValue("Invoice Payment");
    }

    public double calculatePrice() {
        double total;
        total = (ParseDouble(preferences.getString("PAYMENT_AMOUNT", ""))) - (
                ParseDouble(binding.Amount.getEditText().getText().toString().trim()) +
                        ParseDouble(binding.CashAmount.getEditText().getText().toString().trim()) +
                        ParseDouble(binding.InvoiceAmount.getEditText().getText().toString().trim()) +
                        ParseDouble(binding.ChequeAmount.getEditText().getText().toString().trim()));
        return total;
    }

    public double ParseDouble(String str) {
        if (str == null || str.length() <= 0) {
            return 0.0d;
        }
        try {
            return Double.parseDouble(str);
        } catch (Exception unused) {
            return -1.0d;
        }
    }

    public boolean validate() {
        boolean valid = true;
        if (binding.MpesaCard.getVisibility() == View.VISIBLE) {
            if (binding.PhoneNumber.getEditText().getText().toString().trim().equals("") || binding.PhoneNumber.getEditText().getText().toString().trim().length() != 10) {
                binding.PhoneNumber.setError("Please input valid phone number");
                valid = false;
            } else {
                binding.PhoneNumber.setError("");
            }

            if (binding.Amount.getEditText().getText().toString().trim().equals("")) {
                binding.Amount.setError("Please input amount");
                valid = false;
            } else {
                binding.Amount.setError("");
            }
        }

        if (binding.ChequeCard.getVisibility() == View.VISIBLE) {
            if (binding.ChequeNumber.getEditText().getText().toString().trim().equals("")) {
                binding.ChequeNumber.setError("Please input valid cheque number");
                valid = false;
            } else {
                binding.ChequeNumber.setError("");
            }

            if (binding.ChequeAmount.getEditText().getText().toString().trim().equals("")) {
                binding.ChequeAmount.setError("Please input amount");
                valid = false;
            } else {
                binding.ChequeAmount.setError("");
            }

            if (binding.Code.getEditText().getText().toString().trim().equals("")) {
                binding.Code.setError("Please input verification code");
                valid = false;
            } else {
                binding.Code.setError("");
            }

            if (bitmap == null) {
                Toast.makeText(PaymentActivity.this, "Please Capture Cheque Image", Toast.LENGTH_SHORT).show();
                valid = false;
            }

        }

        if (binding.InvoiceCard.getVisibility() == View.VISIBLE) {
            if (binding.InvoiceAmount.getEditText().getText().toString().trim().equals("")) {
                binding.InvoiceAmount.setError("Please input amount");
                valid = false;
            } else {
                binding.InvoiceAmount.setError("");
            }

            if (binding.Code.getEditText().getText().toString().trim().equals("")) {
                binding.Code.setError("Please input verification code");
                valid = false;
            } else {
                binding.Code.setError("");
            }
        }

        if (binding.CashCard.getVisibility() == View.VISIBLE) {
            if (binding.CashAmount.getEditText().getText().toString().trim().equals("")) {
                binding.CashAmount.setError("Please input amount");
                valid = false;
            } else {
                binding.CashAmount.setError("");
            }
        }

        if (ParseDouble(binding.TotalAmountPaid.getText().toString().trim()) != 0) {
            Toast.makeText(PaymentActivity.this, "Make sure total amount is KSH " + preferences.getString("PAYMENT_AMOUNT", ""), Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }
}