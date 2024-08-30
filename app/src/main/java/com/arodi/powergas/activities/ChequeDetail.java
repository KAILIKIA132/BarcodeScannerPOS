package com.arodi.powergas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.DetailAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.databinding.ChequeDetailBinding;
import com.arodi.powergas.entities.ChequeEntity;
import com.arodi.powergas.models.ProductHistory;
import com.arodi.powergas.session.SessionManager;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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

public class ChequeDetail extends AppCompatActivity {
    ChequeDetailBinding binding;
    public ChequeEntity chequeEntity;
    public ArrayList<ProductHistory> productHistory = new ArrayList<>();
    public DetailAdapter detailAdapter;
    HashMap<String, String> user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.cheque_detail);
        user = new SessionManager(ChequeDetail.this).getLoginDetails();
        chequeEntity = getIntent().getParcelableExtra("ChequeInformation");

        populateSales();
        binding.RecyclerView.setHasFixedSize(true);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.Payment.setOnClickListener(view -> makeSale());


    }


    public void populateSales() {
        try {
            JSONArray jSONArray = new JSONArray(chequeEntity.getProducts());
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jsonObject = jSONArray.getJSONObject(i);
                productHistory.add(new ProductHistory(
                        jsonObject.getString("product_id"),
                        jsonObject.getString("code"),
                        jsonObject.getString("name"),
                        jsonObject.getString("price"),
                        jsonObject.getString("quantity"),
                        jsonObject.getString("total")
                ));
            }
            detailAdapter = new DetailAdapter(this, productHistory);
            binding.RecyclerView.setAdapter(this.detailAdapter);
            double total_amount = 0.0d;
            for(ProductHistory model : productHistory){
                total_amount+= Double.parseDouble(model.getTotal());
            }

            binding.TotalAmount.setText("Kshs " + Math.round(total_amount));
            binding.TotalDue.setText("Kshs " + Math.round(total_amount));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeSale() {
        final SweetAlertDialog dialog = new SweetAlertDialog(ChequeDetail.this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitle("Please Wait...");
        dialog.setCancelable(false);
        dialog.show();

        JSONArray jsonArray = new JSONArray();
        JSONArray array = new JSONArray();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("items", new JSONArray(chequeEntity.getProducts()));
            jsonArray.put(jsonObject);
            System.out.println("hfhfhfhfhfhfhuyyyf" + jsonArray.toString());

            JSONObject object = new JSONObject();
            object.put("items", new JSONArray(chequeEntity.getPayments()));
            array.put(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("discount", "0")
                .addFormDataPart("invoice", "0")
                .addFormDataPart("cheque", "0")
                .addFormDataPart("image", "null")
                .addFormDataPart("invoice_id", "")
                .addFormDataPart("cheque_id", chequeEntity.getSale_id())
                .addFormDataPart("discount_id", "")
                .addFormDataPart("json", removeFirstandLast(jsonArray.toString()))
                .addFormDataPart("customer_id", chequeEntity.getCustomer_id())
                .addFormDataPart("distributor_id", user.get(SessionManager.KEY_DISTRIBUTOR_ID))
                .addFormDataPart("town_id", this.chequeEntity.getCity())
                .addFormDataPart("salesman_id", user.get(SessionManager.KEY_SALESMAN_ID))
                .addFormDataPart("paid_by", "")
                .addFormDataPart("vehicle_id", this.user.get(SessionManager.KEY_VEHICLE_ID))
                .addFormDataPart("payment_status", "unpaid")
                .addFormDataPart("shop_id", this.chequeEntity.getShop_id())
                .addFormDataPart("total", this.chequeEntity.getGrand_total())
                .addFormDataPart("payments", removeFirstandLast(array.toString()))
                .addFormDataPart("signature", "")
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "addSale")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException iOException) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(ChequeDetail.this, "No Connection to host", Toast.LENGTH_SHORT).show();
                    System.out.println("errorrr" + iOException.toString());
                });

            }

            public void onResponse(Call call, Response response) {
                runOnUiThread(()->{
                    try {
                        JSONObject jSONObject = new JSONObject(response.body().string());
                        PrintStream printStream = System.out;
                        printStream.println("jsonObjectiiiii" + jSONObject);
                        if (jSONObject.getString(FirebaseAnalytics.Param.SUCCESS).equals("1")) {
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            dialog.setTitle((CharSequence) "SALE ADDED SUCCESSFULLY");
                            dialog.setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismiss();
                                Intent intent = new Intent(ChequeDetail.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            });
                        }else {
                            dialog.dismiss();
                            Toast.makeText(ChequeDetail.this, jSONObject.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        dialog.dismiss();
                        Toast.makeText(ChequeDetail.this, "An error occurred", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });

            }

        });
    }

    public static String removeFirstandLast(String str) {
        return str.substring(1, str.length() - 1);
    }

}