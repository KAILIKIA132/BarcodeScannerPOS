package com.arodi.powergas.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;


public class NetworkState {
    private static NetworkState instance = new NetworkState();
    static Context context;
    ConnectivityManager connectivityManager;
    boolean connected = false;

    public static NetworkState getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }
    
    public boolean isConnected() {
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager!=null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());  // need ACCESS_NETWORK_STATE permission
                connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected() && capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                
            }
            
        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }
}

