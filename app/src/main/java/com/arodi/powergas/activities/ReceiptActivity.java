package com.arodi.powergas.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.arodi.powergas.R;
import com.arodi.powergas.databinding.ActivityPaymentBinding;
import com.arodi.powergas.models.ProductCodeModel;
import com.arodi.powergas.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;
import java.util.Random;

public class ReceiptActivity extends AppCompatActivity {
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
    String productCode,productName,price;

    String StringPaymentArray;
    String StringProductArray;
int quantity;
    String fmt5 = "%6s %6s %6s %6s";
    String fmt2 = "%6s %6s %6s\n";
//    String[] deviceNames;
//    String[] deviceAddresses;
    String[] deviceNames = {};  // Initialize deviceNames as an empty array
    String[] deviceAddresses = {};  // Initialize deviceAddresses as an empty array

    public UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
String grandTotals,customerName;
float total;
String grandUnit;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.receipt);
        Intent intent1 = getIntent();


        product = getIntent().getParcelableExtra("product_data");
        // Retrieve the quantity from shared preferences
        SharedPreferences quantitysharedPreferences = getSharedPreferences("quantityPrefs", MODE_PRIVATE);
        quantity = quantitysharedPreferences.getInt("quantity", 1); // Default to 1 if not found

        SharedPreferences sharedPreferencesTotals = getSharedPreferences("paymenttotalPrefs", MODE_PRIVATE);
        total = sharedPreferencesTotals.getFloat("total", 0.0f); // Default to 0.0 if not found

        Log.d("PaymentActivity", "Retrieved quantity: " + quantity);
        if (product != null) {
            // Use product data
            productCode = product.getCode();
             productName = product.getName();
           price = product.getPrice();
        } else {
            // Handle null case
            Toast.makeText(this, "No product data found!", Toast.LENGTH_SHORT).show();
        }

//        grandTotals = getIntent().getDoubleExtra("grand_totals", 0.0);
        grandUnit = intent1.getStringExtra("grand_unit");
        grandTotals = intent1.getStringExtra("grand_totals");
        customerName = intent1.getStringExtra("customer_name");
        // Initialize SharedPreferences
        preferences = getSharedPreferences("YourPreferencesName", MODE_PRIVATE);
        // Initialize Bluetooth Adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if Bluetooth is enabled, if not, request to enable it
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        // Check and request Bluetooth permissions at runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION},
                    BLUETOOTH_PERMISSION_REQUEST_CODE);
        }

        // List paired devices and show device selection dialog
        showDeviceSelectionDialog();

        // Initialize the spinner with an empty adapter
        Spinner spinner = findViewById(R.id.spinner_bluetooth_devices);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDeviceAddress = deviceAddresses[position];
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(selectedDeviceAddress);
                connectToBluetoothDevice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        findViewById(R.id.print_receipt).setOnClickListener(v -> {
            Toast.makeText(this, "Print Receipt button clicked", Toast.LENGTH_SHORT).show();
            connectToBluetoothDevice();  // Ensure Bluetooth device is connected before printing
        });

    }

    private void ListPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<BluetoothDevice> bondedDevices = this.mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice next : bondedDevices) {
                // List the devices in a toast or log them for the user to select
                Log.d("Paired Devices", "Device: " + next.getName() + " - " + next.getAddress());
            }
        } else {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
        }
    }


    private void printReceipt() {
        new Thread(() -> {
            try {
                if (mBluetoothSocket == null || !mBluetoothSocket.isConnected()) {
                    runOnUiThread(() -> Toast.makeText(ReceiptActivity.this, "Bluetooth socket is not connected", Toast.LENGTH_SHORT).show());
                    return;
                }
                OutputStream outputStream = mBluetoothSocket.getOutputStream();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // Generate a random receipt number (4-digit example)
                Random random = new Random();
                int receiptNumber = 1000 + random.nextInt(9000); // Generates a number between 1000 and 9999

                // Hardcoded values for testing
                String userPlateNo = "ABC123"; // Hardcoded plate number
                String shopName = "Summit Mobile"; // Hardcoded shop name
                String totalAmount = grandUnit; // Hardcoded total amount
                String paymentMethod = "Cash"; // Hardcoded payment method
                String paymentAmount = grandTotals; // Hardcoded payment amount
                String paymentStatus = "Paid"; // Hardcoded payment status


// Constructing the BILL string
                String BILL = "RECEIPT\nSUMMIT MOBILE INVESTMENT LIMITED\n62639-00200 Nairobi 00200 Kenya\nTEL NO: 0748911778\nEmail: summitemobileinvestmentlimited@gmail.com"
                        + "\nRECEIPT NO: " +  receiptNumber + "\nDATE: " + dateFormat.format(new Date()) + "\n";

                BILL += "================================\nCUSTOMER NAME: " + customerName + "\n\n";

// Format for item list header
//                String fmt5 = "%-20s%-10s%-10s%-10s"; // Format for the item list, adjust as necessary
                String fmt5 = "%-8s %-4s %-8s %-8s";


// Assuming StringProductArray contains the list of items in the receipt
                String StringProductArray = productCode+  "  " +   quantity+  "   " +   grandTotals +   "    "   + total + "";
//                        +\nItem 2    1      500.00    500.00";

// Add item list to the BILL
                BILL += String.format(fmt5, "Item", "Qty", "Price", "Total") + "\n--------------------------------\n\n";
                BILL += StringProductArray + "\nTOTAL: " + total + "\n";
                BILL += "================================\nPayments:\n";

// Format for payment list header
                String fmt2 = "%-10s%-8s%-10s"; // Format for the payment list, adjust as necessary

// Assuming StringPaymentArray contains the payment details
                String StringPaymentArray = paymentMethod + "    " + total + "    " + paymentStatus; // Example hardcoded payment data

// Add payment details to the BILL
                BILL += String.format(fmt2, "Method", "Amount", "Status") + "\n--------------------------------\n";
                BILL += StringPaymentArray + "\n--------------------------------\nPayment Status: Paid\n";

// Print or log the generated BILL string
                Log.d("ReceiptActivity", BILL);

                outputStream.write(BILL.getBytes());
                outputStream.flush();

                runOnUiThread(() -> Toast.makeText(ReceiptActivity.this, "Receipt printed successfully", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                Log.e("ReceiptActivity", "Error printing receipt", e);
                runOnUiThread(() -> Toast.makeText(ReceiptActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (this.mBluetoothSocket != null) {
                this.mBluetoothSocket.close();
            }
        } catch (Exception e) {
            Log.e("ReceiptActivity", "Error closing socket", e);
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
            Log.e("ReceiptActivity", "Error closing socket", e);
        }
        setResult(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth, direct to Bluetooth settings
                Toast.makeText(this, "Bluetooth is required, directing to settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Bluetooth Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Bluetooth Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void showDeviceSelectionDialog() {
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a list of paired devices
        deviceNames = new String[bondedDevices.size()];
        deviceAddresses = new String[bondedDevices.size()];
        int i = 0;
        for (BluetoothDevice device : bondedDevices) {
            deviceNames[i] = device.getName();
            deviceAddresses[i] = device.getAddress();
            i++;
        }

        // Update the spinner adapter with the device names
        Spinner spinner = findViewById(R.id.spinner_bluetooth_devices);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void connectToBluetoothDevice() {
        if (mBluetoothDevice == null) {
            Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothSocket.connect();
            Log.d("Bluetooth", "Connected to device");

            // Proceed to print receipt after connection
            printReceipt();
        } catch (IOException e) {
            Log.e("Bluetooth", "Error connecting to device", e);
            Toast.makeText(this, "Error connecting to Bluetooth device", Toast.LENGTH_SHORT).show();
        }
    }

}

//
//public class ReceiptActivity extends AppCompatActivity {
//    ActivityPaymentBinding binding;
//    SharedPreferences preferences;
//    SweetAlertDialog dialog;
//    HashMap<String, String> user;
//    HashMap<String, String> hash_map = new HashMap<>();
//    ProductCodeModel product;
//    String BILL = "";
//    public String isInvoice = "0";
//    public String isCheque = "0";
//
//    BluetoothAdapter mBluetoothAdapter;
//    BluetoothDevice mBluetoothDevice;
//    public BluetoothSocket mBluetoothSocket;
//    public ProgressDialog progressDialog;
//
//    String StringPaymentArray;
//    String StringProductArray;
//
//    String fmt5 = "%6s %6s %6s %6s";
//    String fmt2 = "%6s %6s %6s\n";
//
//    public UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//
//    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = DataBindingUtil.setContentView(this, R.layout.receipt);
//
//        Toast.makeText(this, "ReceiptActivity initialized", Toast.LENGTH_SHORT).show();
//
//        product = getIntent().getParcelableExtra("product_data");
//
//        user = new SessionManager(ReceiptActivity.this).getLoginDetails();
//
//        // Check and request Bluetooth permissions at runtime
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION},
//                    BLUETOOTH_PERMISSION_REQUEST_CODE);
//        }
//
//        findViewById(R.id.print_receipt).setOnClickListener(v -> {
//            Toast.makeText(this, "Print Receipt button clicked", Toast.LENGTH_SHORT).show();
//            printReceipt();
//        });
//
//    }
//
//    private final Handler mHandler = new Handler() {
//        public void handleMessage(Message message) {
//            progressDialog.dismiss();
//            printReceipt();
//        }
//    };
//
//    public void ListPairedDevices() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Set<BluetoothDevice> bondedDevices = this.mBluetoothAdapter.getBondedDevices();
//        if (bondedDevices.size() > 0) {
//            for (BluetoothDevice next : bondedDevices) {
//                Toast.makeText(this, "Paired Device: " + next.getName() + " - " + next.getAddress(), Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        try {
//            if (this.mBluetoothSocket != null) {
//                this.mBluetoothSocket.close();
//            }
//        } catch (Exception e) {
//            Log.e("ReceiptActivity", "Error closing socket", e);
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        try {
//            if (this.mBluetoothSocket != null) {
//                this.mBluetoothSocket.close();
//            }
//        } catch (Exception e) {
//            Log.e("ReceiptActivity", "Error closing socket", e);
//        }
//        setResult(0);
//    }
//
//    private void closeSocket(BluetoothSocket bluetoothSocket) {
//        try {
//            bluetoothSocket.close();
//            Log.d("ReceiptActivity", "Socket closed successfully");
//        } catch (IOException e) {
//            Log.e("ReceiptActivity", "Could not close socket", e);
//        }
//    }
//
//    public void populateSales() {
//        try {
//            StringBuilder sb = new StringBuilder();
//            JSONArray jsonArray = new JSONArray(preferences.getString("PRODUCT_LIST_RECEIPT", ""));
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                sb.append(String.format(fmt5, jsonObject.getString("name"), jsonObject.getString("quantity"), jsonObject.getString("price"), jsonObject.getString("total") + "\n"));
//            }
//            StringProductArray = sb.toString();
//        } catch (Exception e) {
//            Log.e("ReceiptActivity", "Error populating sales", e);
//        }
//    }
//
//    public void printReceipt() {
//        new Thread(() -> {
//            try {
//                if (mBluetoothSocket == null) {
//                    runOnUiThread(() -> Toast.makeText(ReceiptActivity.this, "Bluetooth socket is null", Toast.LENGTH_SHORT).show());
//                    return;
//                }
//
//                OutputStream outputStream = mBluetoothSocket.getOutputStream();
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//
//                BILL = "RECEIPT\nPOWER GAS\nTEL NO: 0748911778\nVAN: " + user.get(SessionManager.KEY_PLATE_NO)
//                        + "\nRECEIPT NO: " + "1" + "\nDATE: " + dateFormat.format(new Date()) + "\n";
//
//                BILL += "================================\nCUSTOMER NAME: " + preferences.getString("SELECTED_CUSTOMER_NAME", "") + "\nSHOP NAME: " + preferences.getString("SHOP_NAME", "") + "\n\n";
//                BILL += String.format(fmt5, "Item", "Qty", "Price", "Total") + "\n--------------------------------\n";
//                BILL += StringProductArray + "\nTOTAL: " + preferences.getString("TOTAL", "") + "\n";
//                BILL += "================================\nPayments:\n";
//                BILL += String.format(fmt2, "Method", "Amount", "Status") + "\n--------------------------------\n";
//                BILL += StringPaymentArray + "\n--------------------------------\nPayment Status: Paid\n";
//
//                outputStream.write(BILL.getBytes());
//                outputStream.flush();
//
//                runOnUiThread(() -> Toast.makeText(ReceiptActivity.this, "Receipt printed successfully", Toast.LENGTH_SHORT).show());
//            } catch (IOException e) {
//                Log.e("ReceiptActivity", "Error printing receipt", e);
//                runOnUiThread(() -> Toast.makeText(ReceiptActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//            }
//        }).start();
//    }
//
//    // Handle permission request result
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted
//                Toast.makeText(this, "Bluetooth Permission granted", Toast.LENGTH_SHORT).show();
//            } else {
//                // Permission denied
//                Toast.makeText(this, "Bluetooth Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//}
