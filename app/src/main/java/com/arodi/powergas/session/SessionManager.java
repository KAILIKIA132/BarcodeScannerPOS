package com.arodi.powergas.session;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.arodi.powergas.activities.Login;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.models.CustomerModel;

import java.util.HashMap;
import java.util.List;


public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    private static final String PREF_NAME = "EDSPref";
    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_VEHICLE_ID = "vehicle_id";
    public static final String KEY_PLATE_NO = "plate_no";
    public static final String KEY_ROUTE_ID = "route_id";
    public static final String KEY_DISCOUNT_ENABLED= "discount_enabled";
    public static final String KEY_DISTRIBUTOR_ID= "distributor_id";
    public static final String KEY_SALESMAN_ID = "salesman_id";
    public static final String KEY_ENABLE_STOCK = "enable_stock";
    
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    /**
     * Create login session
     * */
    public void createLoginSession(String user_id, String username, String email, String first_name,
                                   String last_name, String phone, String avatar, String vehicle_id,
                                   String plate_no, String route_id, String discount_enabled,
                                   String distributor_id, String salesman_id,
//                                   String enable_stock
                                   String stock){
        editor.putString(KEY_USER_ID, user_id);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FIRST_NAME, first_name);
        editor.putString(KEY_LAST_NAME, last_name);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_AVATAR, avatar);
        editor.putString(KEY_VEHICLE_ID, vehicle_id);
        editor.putString(KEY_PLATE_NO, plate_no);
        editor.putString(KEY_ROUTE_ID, route_id);
        editor.putString(KEY_DISCOUNT_ENABLED, discount_enabled);
        editor.putString(KEY_DISTRIBUTOR_ID, distributor_id);
        editor.putString(KEY_SALESMAN_ID, salesman_id);
//        editor.putString(KEY_ENABLE_STOCK, enable_stock);
        editor.commit();
    }
    
    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        if(!this.isLoggedIn()){
            Intent i = new Intent(_context, Login.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
        
    }
    
    
    
    /**
     * Get stored session data
     * */
    public HashMap<String, String> getLoginDetails(){
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, ""));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, ""));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, ""));
        user.put(KEY_FIRST_NAME, pref.getString(KEY_FIRST_NAME, ""));
        user.put(KEY_LAST_NAME, pref.getString(KEY_LAST_NAME, ""));
        user.put(KEY_PHONE, pref.getString(KEY_PHONE, ""));
        user.put(KEY_AVATAR, pref.getString(KEY_AVATAR, ""));
        user.put(KEY_VEHICLE_ID, pref.getString(KEY_VEHICLE_ID, ""));
        user.put(KEY_PLATE_NO, pref.getString(KEY_PLATE_NO, ""));
        user.put(KEY_ROUTE_ID, pref.getString(KEY_ROUTE_ID, ""));
        user.put(KEY_DISCOUNT_ENABLED, pref.getString(KEY_DISCOUNT_ENABLED, ""));
        user.put(KEY_DISTRIBUTOR_ID, pref.getString(KEY_DISTRIBUTOR_ID, ""));
        user.put(KEY_SALESMAN_ID, pref.getString(KEY_SALESMAN_ID,""));
//        user.put(KEY_ENABLE_STOCK, pref.getString(KEY_ENABLE_STOCK,""));
        return user;
    }
    
    /**
     * Clear session details
     * */
    public void logoutUser(){
        Database db = new Database(this._context);
        List<CustomerModel> cus = db.fetch_all_customers();
        System.out.println(cus);
        editor.clear();
        cus.clear();
        editor.commit();
        Intent i = new Intent(_context, Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }
    
    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}