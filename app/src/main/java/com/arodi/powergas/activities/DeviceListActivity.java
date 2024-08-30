package com.arodi.powergas.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.arodi.powergas.R;
import java.util.Set;

public class DeviceListActivity extends Activity {

    public BluetoothAdapter mBluetoothAdapter;

    private AdapterView.OnItemClickListener mDeviceClickListener = (adapterView, view, i, j) -> {
        try {
            DeviceListActivity.this.mBluetoothAdapter.cancelDiscovery();
            String charSequence = ((TextView) view).getText().toString();
            String substring = charSequence.substring(charSequence.length() - 17);
            System.out.println("Device_Address " + substring);
            Bundle bundle = new Bundle();
            bundle.putString("DeviceAddress", substring);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            DeviceListActivity.this.setResult(-1, intent);
            DeviceListActivity.this.finish();
        } catch (Exception unused) {
        }
    };
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(5);
        setContentView(R.layout.device_list);
        setResult(0);
        mPairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        ListView listView = findViewById(R.id.paired_devices);
        listView.setAdapter(mPairedDevicesArrayAdapter);
        listView.setOnItemClickListener(mDeviceClickListener);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice next : bondedDevices) {
                ArrayAdapter<String> arrayAdapter = mPairedDevicesArrayAdapter;
                arrayAdapter.add(next.getName() + "\n" + next.getAddress());
            }
            return;
        }
        this.mPairedDevicesArrayAdapter.add("None Paired");
    }

    public void onDestroy() {
        super.onDestroy();
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}
