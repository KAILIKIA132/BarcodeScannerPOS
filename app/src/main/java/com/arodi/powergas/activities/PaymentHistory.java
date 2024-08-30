package com.arodi.powergas.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.arodi.powergas.R;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityPaymentBinding;
import com.arodi.powergas.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentHistory extends AppCompatActivity {
    ActivityPaymentBinding binding;
    SharedPreferences preferences;
    SweetAlertDialog dialog;
    HashMap<String, String> user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment);
        user = new SessionManager(PaymentHistory.this).getLoginDetails();
        preferences = getSharedPreferences("PAYMENT_HISTORY_SELECTED", Context.MODE_PRIVATE);
        if (preferences.contains("PAYMENT_AMOUNT")){
            binding.TotalAmountPaid.setText(preferences.getString("PAYMENT_AMOUNT",""));

            try {
                JSONArray jsonArray = new JSONArray(preferences.getString("PAYMENT_METHOD",""));
                System.out.println("jsjdjdjdk"+jsonArray);
                for (int i = 0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);


                    if (jsonObject.getString("name").equals("Mpesa Payment")){
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

                    if (jsonObject.getString("name").equals("Cash Payment")){
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

                    if (jsonObject.getString("name").equals("Cheque Payment")){
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

                    if (jsonObject.getString("name").equals("Invoice Payment")){
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

        binding.back.setOnClickListener(v -> onBackPressed());

        binding.Lipa.setOnClickListener(v -> {
            if (validate()){
                dialog = new SweetAlertDialog(PaymentHistory.this, SweetAlertDialog.PROGRESS_TYPE);
                dialog.setCancelable(false);
                dialog.setTitle("Please Wait...");
                dialog.show();

                String phone_number = "254" +(binding.PhoneNumber.getEditText().getText().toString().trim().length() >= 9 ? binding.PhoneNumber.getEditText().getText().toString().trim().substring(binding.PhoneNumber.getEditText().getText().toString().trim().length() - 9) : "");

                makeSale();

            }
        });

    }

    public static String removeFirstandLast(String str) {
        str = str.substring(1, str.length() - 1);
        return str;
    }

    public void makeSale() {
        JSONArray jsonArray = new JSONArray();
        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        HashMap<String, String> hash_map = new HashMap<>();
        String payment_status_selected = "";

        try {
            if (binding.MpesaCard.getVisibility() == View.VISIBLE){
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("paid_by", "Mpesa Payment");
                jsonObject1.put("amount", binding.Amount.getEditText().getText().toString().trim());
                jsonObject1.put("cheque_no", "NULL");
                jsonObject1.put("type","received");
                jsonArray.put(jsonObject1);
                hash_map.put("Mpesa", "Mpesa Payment");
            }

            if (binding.CashCard.getVisibility() == View.VISIBLE){
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("paid_by", "Cash Payment");
                jsonObject2.put("amount", binding.CashAmount.getEditText().getText().toString().trim());
                jsonObject2.put("cheque_no", "NULL");
                jsonObject2.put("type","received");
                jsonArray.put(jsonObject2);
                hash_map.put("Cash", "Cash Payment");
            }

            if (binding.ChequeCard.getVisibility() == View.VISIBLE){
                JSONObject jsonObject3 = new JSONObject();
                jsonObject3.put("paid_by", "Cheque Payment");
                jsonObject3.put("amount", binding.ChequeAmount.getEditText().getText().toString().trim());
                jsonObject3.put("cheque_no", binding.ChequeNumber.getEditText().getText().toString().trim());
                jsonObject3.put("type","pending");
                jsonArray.put(jsonObject3);
                hash_map.put("Cheque", "Cheque Payment");
            }

            if (binding.InvoiceCard.getVisibility() == View.VISIBLE){
                JSONObject jsonObject4 = new JSONObject();
                jsonObject4.put("paid_by", "Invoice Payment");
                jsonObject4.put("amount", binding.InvoiceAmount.getEditText().getText().toString().trim());
                jsonObject4.put("cheque_no", "NULL");
                jsonObject4.put("type","pending");
                jsonArray.put(jsonObject4);
                hash_map.put("Invoice", "Invoice Payment");
            }

            jsonObject.put("items",jsonArray);
            array.put(jsonObject);


            System.out.println("hfhvjhvhvhv"+removeFirstandLast(array.toString()));

        }catch (Exception e){
            e.printStackTrace();
        }

        //only cash
        if(checkCash(hash_map) && !checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
            payment_status_selected = "paid";
        }
        //only mpesa
        if(!checkCash(hash_map) && checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
            payment_status_selected = "paid";
        }
        //only cheque
        if(!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && !checkInvoice(hash_map)) {
            payment_status_selected = "unpaid";
        }
        //only invoice
        if(!checkCash(hash_map) && !checkMpesa(hash_map) && !checkCheque(hash_map) && checkInvoice(hash_map)) {
            payment_status_selected = "unpaid";
        }

        //only cash and mpesa
        if(checkCash(hash_map) && checkMpesa(hash_map) && !checkCheque(hash_map) && !checkInvoice(hash_map)) {
            payment_status_selected = "paid";
        }

        //only cheque and invoice
        if(!checkCash(hash_map) && !checkMpesa(hash_map) && checkCheque(hash_map) && checkInvoice(hash_map)) {
            payment_status_selected = "unpaid";
        }

        //if either cash or mpesa and cheque or invoice output partial
        if((checkCash(hash_map) || checkMpesa(hash_map)) && (checkCheque(hash_map) || checkInvoice(hash_map))) {
            payment_status_selected = "partial";
        }

        System.out.println("payment status "+payment_status_selected);

        System.out.println("hfhfhfhfhf"+preferences.getString("PAYMENT_AMOUNT",""));
        System.out.println("ttghfhfhfhfhf"+user.get(SessionManager.KEY_SALESMAN_ID));
        System.out.println("htyyfhfhfhfhf"+preferences.getString("SALE_ID",""));
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("amount", preferences.getString("PAYMENT_AMOUNT",""))
                .addFormDataPart("salesman_id", user.get(SessionManager.KEY_SALESMAN_ID))
                .addFormDataPart("sale_id", preferences.getString("SALE_ID",""))
                .addFormDataPart("payments", removeFirstandLast(array.toString()))
                .addFormDataPart("payment_status", payment_status_selected)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL+"updateSale")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(PaymentHistory.this, "No Connection to host", Toast.LENGTH_SHORT).show();
                    System.out.println("errorrr" + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        System.out.println("jsonObjectiiiii" + jsonObject);
                        if (jsonObject.getString("success").equals("1")){
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            dialog.setTitle(jsonObject.getString("message"));
                            dialog.setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismiss();
                                Intent intent = new Intent(PaymentHistory.this, SaleHistory.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            });

                        }else {
                            dialog.dismiss();
                            Toast.makeText(PaymentHistory.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        System.out.println("nhdhdhdhdd"+e);
                        dialog.dismiss();
                        Toast.makeText(PaymentHistory.this, "An error occurred", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    public double calculatePrice(){
        double total;
        total= (ParseDouble(preferences.getString("PAYMENT_AMOUNT",""))) - (
                ParseDouble(binding.Amount.getEditText().getText().toString().trim())+
                        ParseDouble(binding.CashAmount.getEditText().getText().toString().trim())+
                        ParseDouble(binding.InvoiceAmount.getEditText().getText().toString().trim())+
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

    public boolean checkCash(HashMap<String, String> hash_map){
        return hash_map.containsValue("Cash Payment");
    }
    public boolean checkMpesa(HashMap<String, String> hash_map){
        return hash_map.containsValue("Mpesa Payment");
    }
    public boolean checkCheque(HashMap<String, String> hash_map){
        return hash_map.containsValue("Cheque Payment");
    }
    public boolean checkInvoice(HashMap<String, String> hash_map){
        return hash_map.containsValue("Invoice Payment");
    }

    public boolean validate(){
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
        }

        if (binding.InvoiceCard.getVisibility() == View.VISIBLE) {
            if (binding.InvoiceAmount.getEditText().getText().toString().trim().equals("")) {
                binding.InvoiceAmount.setError("Please input amount");
                valid = false;
            } else {
                binding.InvoiceAmount.setError("");
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

        if (ParseDouble(binding.TotalAmountPaid.getText().toString().trim()) != 0){
            Toast.makeText(PaymentHistory.this, "Make sure total amount is KSH "+preferences.getString("PAYMENT_AMOUNT",""), Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }
}