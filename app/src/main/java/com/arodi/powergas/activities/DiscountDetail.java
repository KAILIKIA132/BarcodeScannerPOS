package com.arodi.powergas.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.DetailAdapter;
import com.arodi.powergas.adapters.PaymentAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.DiscountDetailBinding;
import com.arodi.powergas.databinding.PaymentDialogBinding;
import com.arodi.powergas.models.HistoryModel;
import com.arodi.powergas.models.PaymentModel;
import com.arodi.powergas.models.ProductHistory;
import com.arodi.powergas.session.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DiscountDetail extends AppCompatActivity {
    DiscountDetailBinding binding;
    public HistoryModel historyModel;
    public ArrayList<ProductHistory> productHistory = new ArrayList<>();
    public DetailAdapter detailAdapter;
    PaymentDialogBinding paymentBinding;
    public PaymentAdapter paymentAdapter;
    public ArrayList<PaymentModel> paymentModel = new ArrayList<>();
    HashMap<String, String> user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.discount_detail);
        user = new SessionManager(DiscountDetail.this).getLoginDetails();
        historyModel = getIntent().getParcelableExtra("DiscountInformation");

        populateSales();
        binding.RecyclerView.setHasFixedSize(true);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(DiscountDetail.this));

        binding.Payment.setOnClickListener(v -> makeSale());
    }


    public void populateSales() {
        try {
            JSONArray jsonArray = new JSONArray(historyModel.getProducts());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ProductHistory model = new ProductHistory(
                        jsonObject.getString("product_id"),
                        jsonObject.getString("code"),
                        jsonObject.getString("name"),
                        jsonObject.getString("price"),
                        jsonObject.getString("quantity"),
                        jsonObject.getString("total"));

                productHistory.add(model);
            }
            detailAdapter = new DetailAdapter(DiscountDetail.this, productHistory);
            binding.RecyclerView.setAdapter(detailAdapter);

            double total_payment = 0.0;
            for (ProductHistory model : productHistory) {
                total_payment += Integer.parseInt(model.getTotal());
            }
            binding.TotalAmount.setText("Kshs " + (Math.round(total_payment)));
            binding.TotalDue.setText("Kshs " + (Math.round(total_payment)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void makeSale() {
        final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(DiscountDetail.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitle("Please Wait...");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "getPaymentMethodsJson/" + historyModel.getCustomer_id())
                .method("GET", null)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    sweetAlertDialog.dismiss();
                    Toast.makeText(DiscountDetail.this, "No Connection to host", Toast.LENGTH_SHORT).show();
                    System.out.println("errorrr" + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        sweetAlertDialog.dismiss();
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        System.out.println("jsonObjectiiiii" + jsonArray);
                        showPayment(jsonArray);

                    } catch (Exception e) {
                        System.out.println("nhdhdhdhdd" + e);
                        Toast.makeText(DiscountDetail.this, "An error occurred", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    public void showPayment(JSONArray array) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(DiscountDetail.this, R.style.CustomBottomSheetDialogTheme);
        paymentBinding = DataBindingUtil.inflate(LayoutInflater.from(DiscountDetail.this), R.layout.payment_dialog, null, false);
        bottomSheetDialog.setContentView(paymentBinding.getRoot());

        paymentBinding.cancel.setOnClickListener(view -> bottomSheetDialog.dismiss());
        paymentBinding.RecyclerView.setHasFixedSize(true);
        paymentBinding.RecyclerView.setLayoutManager(new GridLayoutManager(DiscountDetail.this, 3));

        try {
            paymentBinding.TotalPrice.setText("Kshs " +historyModel.getGrand_total());
            paymentBinding.GrandTotal.setText("Kshs " +historyModel.getGrand_total());


            paymentModel.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                PaymentModel model = new PaymentModel(
                        jsonObject.getString("id"),
                        jsonObject.getString("name"));
                paymentModel.add(model);
            }

            paymentAdapter = new PaymentAdapter(DiscountDetail.this, paymentModel);
            paymentBinding.RecyclerView.setAdapter(paymentAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

        paymentBinding.Proceed.setOnClickListener(v -> {
            if (paymentModel.size() != 0) {
                List<PaymentModel> list = paymentAdapter.getSelectedItems();
                if (list.size() < 1 || list.size() > 2) {
                    Toast.makeText(DiscountDetail.this, "Please select at most 2 payment methods", Toast.LENGTH_LONG).show();
                } else {
                    JSONArray jsonArray = new JSONArray();
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("items", new JSONArray(historyModel.getProducts()));
                        jsonArray.put(jsonObject);
                        System.out.println("hfhfhfhfhfhfhuyyyf" + jsonArray.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SharedPreferences preferences = getSharedPreferences("PAYMENT_SELECTED", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("PAYMENT_AMOUNT", historyModel.getGrand_total());
                    editor.putString("TOWN_ID", historyModel.getCity());
                    editor.putString("PRODUCT_LIST", removeFirstandLast(jsonArray.toString()));
                    editor.putString("CUSTOMER_ID", historyModel.getCustomer_id());
                    editor.putString("SHOP_ID", historyModel.getShop_id());
                    editor.putString("CUSTOMER_PHONE", historyModel.getPhone());
                    editor.putString("TOTAL", historyModel.getGrand_total());
                    editor.putString("PAYMENT_METHOD", new Gson().toJson(list));
                    editor.putString("PRODUCT_LIST_RECEIPT", new Gson().toJson(new Database(DiscountDetail.this).fetch_sales(historyModel.getCustomer_id())));
                    editor.putString("SELECTED_CUSTOMER_NAME", historyModel.getCustomer());
                    editor.putString("SHOP_NAME", historyModel.getShop_name());
                    editor.putString("DISCOUNT_ID", historyModel.getSale_id());
                    editor.apply();

                    bottomSheetDialog.dismiss();
                    startActivity(new Intent(DiscountDetail.this, PaymentActivity.class));
                }
            } else {
                Toast.makeText(DiscountDetail.this, "Please select at most 2 payment methods", Toast.LENGTH_LONG).show();
            }
        });


        bottomSheetDialog.show();
    }

    public static String removeFirstandLast(String str) {
        str = str.substring(1, str.length() - 1);
        return str;
    }
}