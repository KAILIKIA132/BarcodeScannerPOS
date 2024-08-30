package com.arodi.powergas.activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.DetailAdapter;
import com.arodi.powergas.adapters.PaymentAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.databinding.HistoryDetailBinding;
import com.arodi.powergas.databinding.PaymentDialog2Binding;
import com.arodi.powergas.models.HistoryModel;
import com.arodi.powergas.models.PaymentModel;
import com.arodi.powergas.models.ProductHistory;
import com.arodi.powergas.session.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.internal.NavigationMenu;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryDetail extends AppCompatActivity implements Runnable {
    HistoryDetailBinding binding;
    public HistoryModel historyModel;
    public ArrayList<ProductHistory> productHistory = new ArrayList<>();
    public DetailAdapter detailAdapter;
    PaymentDialog2Binding paymentBinding;
    public PaymentAdapter paymentAdapter;
    public ArrayList<PaymentModel> paymentModel = new ArrayList<>();
    HashMap<String, String> user;

    String BILL = "";

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    public BluetoothSocket mBluetoothSocket;
    public ProgressDialog progressDialog;

    String StringPaymentArray;
    String StringProductArray;

    String fmt5 = "%6s %6s %6s %6s";
    String fmt2 = "%6s %6s %6s\n";

    public UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.history_detail);
        user = new SessionManager(HistoryDetail.this).getLoginDetails();
        historyModel = getIntent().getParcelableExtra("HistoryInformation");

        if (historyModel.getPayment_status().equals("unpaid")) {
            System.out.println("jjgjfjfjg"+historyModel.getPayments());
            try {
                double total_due = 0.0;
                JSONArray jsonArray = new JSONArray(historyModel.getPayments());
                for (int i =0; i<jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("type").equals("pending")){
                        total_due += Double.parseDouble(jsonObject.getString("amount"));
                    }
                }
                System.out.println("jcjcjcjcjcc"+total_due);
                double finalTotal_due = total_due;
                if (finalTotal_due > 0){
                    binding.Payment.setVisibility(View.VISIBLE);
                    binding.Payment.setOnClickListener(v -> makeSale(historyModel.getGrand_total()));
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        if (historyModel.getPayment_status().equals("partial")) {
            System.out.println("jjgjfjfjg"+historyModel.getPayments());
            try {
                double total_due = 0.0;
                JSONArray jsonArray = new JSONArray(historyModel.getPayments());
                for (int i =0; i<jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("type").equals("pending")){
                        total_due += Double.parseDouble(jsonObject.getString("amount"));
                    }
                }
                System.out.println("jcjcjcjcjcc"+total_due);
                double finalTotal_due = total_due;
                if (finalTotal_due > 0){
                    binding.Payment.setVisibility(View.VISIBLE);
                    binding.Payment.setOnClickListener(v -> makeSale(String.valueOf(finalTotal_due)));
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        if (historyModel.getPayment_status().equals("paid")){
            binding.Receipt.setVisibility(View.VISIBLE);
            binding.Receipt.setOnClickListener(v -> {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    Toast.makeText(HistoryDetail.this, "Cant connect to printer", Toast.LENGTH_LONG).show();
                } else if (!mBluetoothAdapter.isEnabled()) {
                    startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 2);
                } else {
                    ListPairedDevices();
                    startActivityForResult(new Intent(HistoryDetail.this, DeviceListActivity.class), 1);
                }
            });
        }


        binding.PDFDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_sale:
                        SharedPreferences sharedPreferences = getSharedPreferences("CUSTOMER_SHOPPING_DATA", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("CUSTOMER_ID", historyModel.getCustomer_id());
                        editor.putString("CUSTOMER_NAME", historyModel.getShop_name());
                        editor.putString("LAT", historyModel.getLat());
                        editor.putString("LNG", historyModel.getLng());
                        editor.putString("TOWN_ID", historyModel.getCity());
                        editor.putString("SHOP_ID", historyModel.getShop_id());
                        editor.putString("CUSTOMER_PHONE", historyModel.getPhone());
                        editor.putString("SELECTED_CUSTOMER_NAME", historyModel.getCustomer());
                        editor.apply();

                        startActivity(new Intent(HistoryDetail.this, ShopActivity.class));
                        break;
                }
                return true;
            }
        });

        populateSales();
        populatePayments();
        binding.RecyclerView.setHasFixedSize(true);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(HistoryDetail.this));
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

    public void run() {
        try {
            this.mBluetoothSocket = this.mBluetoothDevice.createRfcommSocketToServiceRecord(this.applicationUUID);
            this.mBluetoothAdapter.cancelDiscovery();
            this.mBluetoothSocket.connect();
            this.mHandler.sendEmptyMessage(0);
        } catch (IOException e) {
            System.out.println("CouldNotConnectToSocket" + e);
            this.progressDialog.dismiss();
            closeSocket(this.mBluetoothSocket);
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
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
            progressDialog = ProgressDialog.show(HistoryDetail.this, "Connecting...", mBluetoothDevice.getName() + " : " + mBluetoothDevice.getAddress(), true, true);
            new Thread(HistoryDetail.this).start();
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
                sb.append(String.format(fmt5, new Object[]{jsonObject.getString("name"), jsonObject.getString("quantity"), jsonObject.getString("price"), jsonObject.getString("total") +"\n"}));
            }
            StringProductArray = String.valueOf(sb);
            detailAdapter = new DetailAdapter(HistoryDetail.this, productHistory);
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

    public void populatePayments() {
        try {
            StringBuilder sb = new StringBuilder();
            System.out.println("hfhfhfhfg"+historyModel.getPayments());
            JSONArray jsonArray = new JSONArray(historyModel.getPayments());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                 sb.append(String.format(fmt2, new Object[]{jsonObject.getString("paid_by"), jsonObject.getString("amount"), jsonObject.getString("type")}));
            }
            StringPaymentArray = String.valueOf(sb);
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

                    BILL = "RECEIPT \nPOWER GAS \nTEL NO: 0748911778 \nVAN :" +user.get(SessionManager.KEY_PLATE_NO)
                    +"\nRECEIPT NO: "+historyModel.getSale_id()+"\nDATE : "+simpleDateFormat.format(new Date())+"\n";

                    BILL = BILL + "================================\n CUSTOMER NAME: "+ historyModel.getCustomer()+"\nSHOP NAME: "+historyModel.getShop_name()+"\n\n";
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(BILL);

                    sb2.append(String.format(fmt5, new Object[]{"Item", "Qty", "Price", "Total"}));
                    BILL = sb2.toString();
                    BILL = BILL + "\n--------------------------------\n";;
                    BILL = BILL + StringProductArray;

                    BILL = BILL + "\nTOTAL : "+historyModel.getGrand_total()+"\n";
                    BILL = BILL + "================================\n";
                    BILL = BILL + "\nPayments : \n";
                    BILL = BILL + String.format(fmt2, new Object[]{"Method", "Amount", "Status"});
                    BILL = BILL + "\n--------------------------------\n";
                    BILL = BILL + StringPaymentArray;
                    BILL = BILL + "\n--------------------------------\n";
                    BILL = BILL + "Payment Status "+historyModel.getPayment_status()+"\n";
                    BILL = BILL + "\n--------------------------------";
                    BILL = BILL + "\nServed By :" + user.get(SessionManager.KEY_USERNAME);
                    BILL = BILL + "\nON : " + simpleDateFormat.format(new Date());
                    BILL = BILL + "\n" + "POWER GAS \nRAFIKI JIKONI \n";
                    BILL = BILL + "--------------------------------\n";
                    BILL = BILL + "\n\n";
                    outputStream.write(BILL.getBytes(), 0, BILL.getBytes().length);
                    outputStream.write(BILL.getBytes(), 0, BILL.getBytes().length);
                } catch (Exception e) {
                    Log.e("MainActivity", "Exe ", e);
                }
            }
        }.start();
    }

    public void makeSale(String grand_total) {
        final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(HistoryDetail.this, SweetAlertDialog.PROGRESS_TYPE);
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
                    Toast.makeText(HistoryDetail.this, "No Connection to host", Toast.LENGTH_SHORT).show();
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
                        showPayment(jsonArray, grand_total);

                    } catch (Exception e) {
                        System.out.println("nhdhdhdhdd" + e);
                        Toast.makeText(HistoryDetail.this, "An error occurred", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    public void showPayment(JSONArray array, String grand_total) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HistoryDetail.this, R.style.CustomBottomSheetDialogTheme);
        paymentBinding = DataBindingUtil.inflate(LayoutInflater.from(HistoryDetail.this), R.layout.payment_dialog2, null, false);
        bottomSheetDialog.setContentView(paymentBinding.getRoot());

        paymentBinding.cancel.setOnClickListener(view -> bottomSheetDialog.dismiss());
        paymentBinding.RecyclerView.setHasFixedSize(true);
        paymentBinding.RecyclerView.setLayoutManager(new GridLayoutManager(HistoryDetail.this, 3));

        try {
            paymentBinding.TotalPrice.setText(grand_total);
            paymentBinding.GrandTotal.setText(grand_total);


            paymentModel.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                PaymentModel model = new PaymentModel(
                        jsonObject.getString("id"),
                        jsonObject.getString("name"));
                paymentModel.add(model);
            }

            paymentAdapter = new PaymentAdapter(HistoryDetail.this, paymentModel);
            paymentBinding.RecyclerView.setAdapter(paymentAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

        paymentBinding.Proceed.setOnClickListener(v -> {
            if (paymentModel.size() != 0) {
                List<PaymentModel> list = paymentAdapter.getSelectedItems();
                if (list.size() < 1 || list.size() > 2) {
                    Toast.makeText(HistoryDetail.this, "Please select at most 2 payment methods", Toast.LENGTH_LONG).show();
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
                    SharedPreferences preferences = getSharedPreferences("PAYMENT_HISTORY_SELECTED", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("PAYMENT_AMOUNT", grand_total);
                    editor.putString("SALE_ID", historyModel.getSale_id());
                    editor.putString("PAYMENT_METHOD", new Gson().toJson(list));
                    editor.apply();

                    startActivity(new Intent(HistoryDetail.this, PaymentHistory.class));
                }
            } else {
                Toast.makeText(HistoryDetail.this, "Please select at most 2 payment methods", Toast.LENGTH_LONG).show();
            }
        });


        bottomSheetDialog.show();
    }
}