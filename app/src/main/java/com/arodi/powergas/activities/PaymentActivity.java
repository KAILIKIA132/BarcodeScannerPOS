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

    public UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment);
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
        //only cash
        if (checkCash(hash_map) && !checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
            submitSale("", "");
        }
        //only mpesa
        if (!checkCash(hash_map) && checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
            submitSale("", "");
        }
        //only cheque
        if (!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && !checkInvoice(hash_map)) {
            confirmCode();
        }
        //only invoice
        if (!checkCash(hash_map) && !checkMpesa(hash_map) && !checkCheque(hash_map) && checkInvoice(hash_map)) {
            confirmCode();
        }

        //only cash and mpesa
        if (checkCash(hash_map) && checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
            submitSale("", "");
        }

        //only cheque and invoice
        if (!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && checkInvoice(hash_map)) {
            confirmCode();
        }

        //if either cash or mpesa and cheque or invoice output partial
        if ((checkCash(hash_map) || checkMpesa(hash_map)) && (checkCheque(hash_map) || checkInvoice(hash_map))) {
            confirmCode();
        }

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
                            submitSale(object.getString("image_url"), object.getString("cheque_image"));
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

    public void submitSale(String signature, String cheque_image) {
        String payment_status_selected = "";

        JSONArray jsonArray = new JSONArray();
        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        StringBuilder sb = new StringBuilder();

        try {
            if (binding.MpesaCard.getVisibility() == View.VISIBLE) {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("paid_by", "Mpesa Payment");
                jsonObject1.put("amount", binding.Amount.getEditText().getText().toString().trim());
                jsonObject1.put("cheque_no", "NULL");
                jsonObject1.put("type", "received");
                jsonArray.put(jsonObject1);
                hash_map.put("Mpesa", "Mpesa Payment");
                sb.append(String.format(fmt2, new Object[]{"Mpesa Payment", binding.Amount.getEditText().getText().toString().trim(), "received"}));
            }

            if (binding.CashCard.getVisibility() == View.VISIBLE) {
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("paid_by", "Cash Payment");
                jsonObject2.put("amount", binding.CashAmount.getEditText().getText().toString().trim());
                jsonObject2.put("cheque_no", "NULL");
                jsonObject2.put("type", "received");
                jsonArray.put(jsonObject2);
                hash_map.put("Cash", "Cash Payment");
                sb.append(String.format(fmt2, new Object[]{"Cash Payment", binding.CashAmount.getEditText().getText().toString().trim(), "received"}));
            }

            if (binding.ChequeCard.getVisibility() == View.VISIBLE) {
                JSONObject jsonObject3 = new JSONObject();
                jsonObject3.put("paid_by", "Cheque Payment");
                jsonObject3.put("amount", binding.ChequeAmount.getEditText().getText().toString().trim());
                jsonObject3.put("cheque_no", binding.ChequeNumber.getEditText().getText().toString().trim());
                jsonObject3.put("type", "unconfirmed");
                jsonArray.put(jsonObject3);
                hash_map.put("Cheque", "Cheque Payment");
                isCheque = "1";
            }

            if (binding.InvoiceCard.getVisibility() == View.VISIBLE) {
                JSONObject jsonObject4 = new JSONObject();
                jsonObject4.put("paid_by", "Invoice Payment");
                jsonObject4.put("amount", binding.InvoiceAmount.getEditText().getText().toString().trim());
                jsonObject4.put("cheque_no", "NULL");
                jsonObject4.put("type", "unconfirmed");
                jsonArray.put(jsonObject4);
                hash_map.put("Invoice", "Invoice Payment");
                isInvoice = "1";
            }

            jsonObject.put("items", jsonArray);
            array.put(jsonObject);

            StringPaymentArray = String.valueOf(sb);

            System.out.println("hfhvjhvhvhvfgg" + removeFirstandLast(array.toString()));

        } catch (Exception e) {
            e.printStackTrace();
        }


        //only cash
        if (checkCash(hash_map) && !checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
            payment_status_selected = "paid";
        }
        //only mpesa
        if (!checkCash(hash_map) && checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
            payment_status_selected = "paid";
        }
        //only cheque
        if (!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && !checkInvoice(hash_map)) {
            payment_status_selected = "unpaid";
        }
        //only invoice
        if (!checkCash(hash_map) && !checkMpesa(hash_map) && !checkCheque(hash_map) && checkInvoice(hash_map)) {
            payment_status_selected = "unpaid";
        }

        //only cash and mpesa
        if (checkCash(hash_map) && checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
            payment_status_selected = "paid";
        }

        //only cheque and invoice
        if (!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && checkInvoice(hash_map)) {
            payment_status_selected = "unpaid";
        }

        //if either cash or mpesa and cheque or invoice output partial
        if ((checkCash(hash_map) || checkMpesa(hash_map)) && (checkCheque(hash_map) || checkInvoice(hash_map))) {
            payment_status_selected = "partial";
        }


        System.out.println("hhhuddydgdhdh" + array);
        System.out.println("payment status " + payment_status_selected);
        String finalPayment_status_selected = payment_status_selected;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("discount", "0")
                .addFormDataPart("invoice", isInvoice)
                .addFormDataPart("invoice_id", "")
                .addFormDataPart("cheque_id", "")
                .addFormDataPart("image", cheque_image)
                .addFormDataPart("cheque", isCheque)
                .addFormDataPart("discount_id", preferences.getString("DISCOUNT_ID", ""))
                .addFormDataPart("json", preferences.getString("PRODUCT_LIST", ""))
                .addFormDataPart("customer_id", preferences.getString("CUSTOMER_ID", ""))
                .addFormDataPart("distributor_id", user.get(SessionManager.KEY_DISTRIBUTOR_ID))
                .addFormDataPart("town_id", preferences.getString("TOWN_ID", ""))
                .addFormDataPart("salesman_id", user.get(SessionManager.KEY_SALESMAN_ID))
                .addFormDataPart("paid_by", preferences.getString("PAYMENT_METHOD", ""))
                .addFormDataPart("vehicle_id", user.get(SessionManager.KEY_VEHICLE_ID))
                .addFormDataPart("payment_status", payment_status_selected)
                .addFormDataPart("shop_id", preferences.getString("SHOP_ID", ""))
                .addFormDataPart("total", preferences.getString("TOTAL", ""))
                .addFormDataPart("payments", removeFirstandLast(array.toString()))
                .addFormDataPart("signature", signature)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "addSale")
                .post(requestBody)
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
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        System.out.println("jsonObjectiiiii" + jsonObject);
                        if (jsonObject.getString("success").equals("1")) {
                            dialog.dismiss();
                            new Database(PaymentActivity.this).clear_customer_sales(preferences.getString("CUSTOMER_ID", ""));

                            if (finalPayment_status_selected.equals("paid")) {
                                new AlertDialog.Builder(PaymentActivity.this)
                                        .setTitle("SALE ADDED SUCCESSFULLY")
                                        .setCancelable(false)
                                        .setMessage("GENERATE RECEIPT")
                                        .setPositiveButton("GENERATE", (dialogInterface, i) -> {

                                            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                            if (mBluetoothAdapter == null) {
                                                Toast.makeText(PaymentActivity.this, "Cant connect to printer", Toast.LENGTH_LONG).show();
                                            } else if (!mBluetoothAdapter.isEnabled()) {
                                                startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 2);
                                            } else {
                                                ListPairedDevices();
                                                startActivityForResult(new Intent(PaymentActivity.this, DeviceListActivity.class), 1);
                                            }
                                        })
                                        .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                                            dialogInterface.dismiss();
                                            goBackToSales();
                                        }).show();

                            } else {
                                goBackToSales();
                                Toast.makeText(PaymentActivity.this, "SALE ADDED SUCCESSFULLY", Toast.LENGTH_LONG).show();
                            }

                        } else if(jsonObject.getString("success").equals("12")){
                            new Database(PaymentActivity.this).clear_customer_sales(preferences.getString("CUSTOMER_ID", ""));
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            dialog.setTitle("Invoice sale added successfully, Please contact system admin for approval.");
                            dialog.setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismiss();
                                Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            });
                        } else if(jsonObject.getString("success").equals("17")){
                            new Database(PaymentActivity.this).clear_customer_sales(preferences.getString("CUSTOMER_ID", ""));
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            dialog.setTitle("Cheque sale added successfully, Please contact system admin for approval.");
                            dialog.setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismiss();
                                Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            });
                        }else {
                            dialog.dismiss();
                            Toast.makeText(PaymentActivity.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        dialog.dismiss();
                        Toast.makeText(PaymentActivity.this, "An error occurred", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }

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