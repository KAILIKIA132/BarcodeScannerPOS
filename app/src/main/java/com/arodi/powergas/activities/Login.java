package com.arodi.powergas.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.arodi.powergas.R;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.databinding.ActivityLoginBinding;
import com.arodi.powergas.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Login extends AppCompatActivity {
    ActivityLoginBinding binding;
    SessionManager session;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        session = new SessionManager(getApplicationContext());

        binding.LoginButton.setOnClickListener(v -> {
            if (validate()) {
                login();
            }
        });

        sharedPreferences = getSharedPreferences("LOGIN_DETAILS", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("email") && sharedPreferences.contains("password")) {
            binding.LoginName.getEditText().setText(sharedPreferences.getString("email", ""));
            binding.LoginPassword.getEditText().setText(sharedPreferences.getString("password", ""));
            binding.RememberMe.setChecked(true);

        }
    }



//    public void login() {
//
//        final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(Login.this, SweetAlertDialog.PROGRESS_TYPE);
//        sweetAlertDialog.setTitle("Please Wait...");
//        sweetAlertDialog.setCancelable(false);
//        sweetAlertDialog.show();
//
//        String email = binding.LoginName.getEditText().getText().toString().trim();
//        String password = binding.LoginPassword.getEditText().getText().toString().trim();
//
//        Log.d("Email",email);
//        Log.d("Password",password);
//
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .build();
//        Log.d("Testing1",password);
//
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("email", email)
//                .addFormDataPart("password", password)
//                .build();
//        Log.d("Testing2",password);
//
//        Request request = new Request.Builder()
//                .url(BaseURL.ROUTE_URL + "authentication/loginEndpoint.php?email=" + email + "&password=" + password + "&action=login_user")
//                .post(requestBody)
//                .build();
//        Log.d("Testing3",(BaseURL.ROUTE_URL + "authentication/loginEndpoint.php?email=" + email + "&password=" + password + "&action=login_user")
//);
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(() -> {
//                    sweetAlertDialog.dismiss();
//                    Toast.makeText(Login.this, "No Connection to host", Toast.LENGTH_SHORT).show();
//                    System.out.println("errorrr" + e.toString());
//                    Log.d("Testing4",password);
//
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//                new Thread(() -> {
//                    try {
//                        // Ensure the response body is not null
//                        ResponseBody responseBody = response.body();
//                        if (responseBody == null) {
//                            runOnUiThread(() -> Toast.makeText(Login.this, "Response body is null", Toast.LENGTH_LONG).show());
//                            return;
//                        }
//                        // Get the response body string
//                        String responseBodyString = responseBody.string();
//                        Log.d("Testing5", password);
//                        Log.d("ResponseBody", responseBodyString); // Log the response body for debugging
//
//                        JSONObject jsonObject = new JSONObject(responseBodyString);
//                        System.out.println("jsonObjectiiiii" + jsonObject);
//                        Log.d("Testing6", password);
//
//                        // Check for success
//                        if (jsonObject.getString("success").equals("1")) {
//                            Log.d("Testing7", password);
//
//                            JSONObject userObject = jsonObject.getJSONObject("user");
//                            Log.d("Testing8", password);
//
//                            runOnUiThread(() -> {
//                                SharedPreferences.Editor editor = sharedPreferences.edit();
//                                if (binding.RememberMe.isChecked()) {
//                                    editor.putString("email", binding.LoginName.getEditText().getText().toString().trim());
//                                    editor.putString("password", binding.LoginPassword.getEditText().getText().toString().trim());
//                                } else {
//                                    editor.clear();
//                                }
//                                editor.apply();
//
//                                // Create login session with user details
//                                try {
//                                    session.createLoginSession(
//                                            userObject.getString("id"),
//                                            userObject.getString("username"),
//                                            userObject.getString("email"),
//                                            userObject.getString("first_name"),
//                                            userObject.getString("last_name"),
//                                            userObject.getString("phone"),
//                                            userObject.getString("avatar"),
//                                            userObject.optString("vehicle_id", ""),
//                                            userObject.optString("plate_no", ""),
//                                            userObject.optString("route_id", ""),
//                                            userObject.optString("discount_enabled", ""),
//                                            userObject.optString("distributor_id", ""),
//                                            userObject.optString("salesman_id", ""),
//                                            userObject.optString("stock", "")
//                                    );
//                                } catch (JSONException e) {
//                                    throw new RuntimeException(e);
//                                }
//                                Log.d("Response1", session.toString());
//
//                                // Start the appropriate activity based on group_id
//                                Intent intent;
//                                try {
//                                    if (userObject.getString("group_id").equals("13")) {
//                                        intent = new Intent(Login.this, MainActivity.class);
//                                    } else {
//                                        intent = new Intent(Login.this, PeopleActivity.class);
//                                    }
//                                } catch (JSONException e) {
//                                    throw new RuntimeException(e);
//                                }
//                                startActivity(intent);
//                            });
//                        } else {
//                            // Show the message from the API response
//                            String message = jsonObject.getString("message");
//                            Log.d("API Response", message);
//                            runOnUiThread(() -> Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show());
//                        }
//                    } catch (Exception e) {
//                        Log.e("Exception", "Error processing response", e);
//                        runOnUiThread(() -> Toast.makeText(Login.this, "An error occurred while processing your request. Please try again.", Toast.LENGTH_LONG).show());
//                    }
//                }).start();
//            }
//
////            public void onResponse(Call call, Response response) {
////                runOnUiThread(() -> {
////                    sweetAlertDialog.dismiss();  // Dismiss the dialog as soon as we start processing the response
////                    try {
////                        // Get the response body string
////                        Log.d("Testing5",password);
////
////                        String responseBodyString = response.body().string();
////                        JSONObject jsonObject = new JSONObject(responseBodyString);
////                        System.out.println("jsonObjectiiiii" + jsonObject);
////                        Log.d("Testing6",password);
////
////
////                        // Check for success
////                        if (jsonObject.getString("success").equals("1")) {
////                            Log.d("Testing7",password);
////
////                            JSONObject userObject = jsonObject.getJSONObject("user");
////                            Log.d("Testing8",password);
////
////                            if (binding.RememberMe.isChecked()) {
////                                SharedPreferences.Editor editor = sharedPreferences.edit();
////                                editor.putString("email", binding.LoginName.getEditText().getText().toString().trim());
////                                editor.putString("password", binding.LoginPassword.getEditText().getText().toString().trim());
////                                editor.apply();
////                            } else {
////                                SharedPreferences.Editor editor = sharedPreferences.edit();
////                                editor.clear();
////                                editor.apply();
////                            }
////
////                            // Create login session with user details
////                            session.createLoginSession(
////                                    userObject.getString("id"),
////                                    userObject.getString("username"),
////                                    userObject.getString("email"),
////                                    userObject.getString("first_name"),
////                                    userObject.getString("last_name"),
////                                    userObject.getString("phone"),
////                                    userObject.getString("avatar"),
////                                    userObject.optString("vehicle_id", ""),
////                                    userObject.optString("plate_no", ""),
////                                    userObject.optString("route_id", ""),
////                                    userObject.optString("discount_enabled", ""),
////                                    userObject.optString("distributor_id", ""),
////                                    userObject.optString("salesman_id", ""),
////                                    userObject.optString("stock", "")
////                            );
////Log.d("Response1",session.toString());
////                            // Start the appropriate activity based on group_id
////                            if (userObject.getString("group_id").equals("13")) {
////                                startActivity(new Intent(Login.this, MainActivity.class));
////                            } else {
////                                startActivity(new Intent(Login.this, PeopleActivity.class));
////                            }
////
////                        } else {
////                            // Show the message from the API response
////                            Toast.makeText(Login.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
////                        }
////
////                    } catch (Exception e) {
////                        System.out.println("nhdhdhdhdd" + e);
////                        Toast.makeText(Login.this, "An error occurred", Toast.LENGTH_LONG).show();
////                        e.printStackTrace();
////                    }
////                });
////            }
//
////            public void onResponse(Call call, Response response) {
////                runOnUiThread(() -> {
////                    try {
////                        JSONObject jsonObject = new JSONObject(response.body().string());
////                        System.out.println("jsonObjectiiiii" + jsonObject);
////                        if (jsonObject.getString("success").equals("1")){
////                            JSONObject object = new JSONObject(jsonObject.getString("user"));
////
////                            if (binding.RememberMe.isChecked()) {
////                                SharedPreferences.Editor editor = sharedPreferences.edit();
////                                editor.putString("email", binding.LoginName.getEditText().getText().toString().trim());
////                                editor.putString("password", binding.LoginPassword.getEditText().getText().toString().trim());
////                                editor.apply();
////                            }else {
////                                SharedPreferences.Editor editor = sharedPreferences.edit();
////                                editor.clear();
////                                editor.apply();
////                            }
////
////                            sweetAlertDialog.dismiss();
////
////
////                            if (object.getString("group_id").equals("13")) {
////                                session.createLoginSession(
////                                        object.getString("id"),
////                                        object.getString("username"),
////                                        object.getString("email"),
////                                        object.getString("first_name"),
////                                        object.getString("last_name"),
////                                        object.getString("phone"),
////                                        object.getString("avatar"),
////                                        object.getString("vehicle_id"),
////                                        object.getString("plate_no"),
////                                        object.getString("route_id"),
////                                        object.getString("discount_enabled"),
////                                        object.getString("distributor_id"),
////                                        object.getString("salesman_id"),
////                                        object.getString("stock"));
////
////                                startActivity(new Intent(Login.this, MainActivity.class));
////                            }else {
////                                session.createLoginSession(
////                                        object.getString("id"),
////                                        object.getString("username"),
////                                        object.getString("email"),
////                                        object.getString("first_name"),
////                                        object.getString("last_name"),
////                                        object.getString("phone"),
////                                        object.getString("avatar"),
////                                        "",
////                                        "",
////                                        "",
////                                        "",
////                                        "",
////                                        "",
////                                        "");
////
////                                startActivity(new Intent(Login.this, PeopleActivity.class));
////                            }
////
////
////                        }else {
////                            sweetAlertDialog.dismiss();
////                            Toast.makeText(Login.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
////                        }
////
////                    } catch (Exception e) {
////                        System.out.println("nhdhdhdhdd"+e);
////                        sweetAlertDialog.dismiss();
////                        Toast.makeText(Login.this, "An error occurred", Toast.LENGTH_LONG).show();
////                        e.printStackTrace();
////                    }
////
////
////                });
////
////            }
//        });
//    }

    public void login() {
        final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(Login.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitle("Please Wait...");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();

        String email = binding.LoginName.getEditText().getText().toString().trim();
        String password = binding.LoginPassword.getEditText().getText().toString().trim();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("action", "login")
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL + "auth.php?action=login")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    sweetAlertDialog.dismiss();
                    Toast.makeText(Login.this, "No Connection to host", Toast.LENGTH_SHORT).show();
                    Log.e("LoginError", "Failure: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> sweetAlertDialog.dismiss());

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(Login.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String responseBodyString = response.body().string();
                    Log.d("ResponseBody", responseBodyString);

                    JSONObject jsonObject = new JSONObject(responseBodyString);

                    if (jsonObject.has("error")) {
                        String error = jsonObject.getString("error");
                        int status = jsonObject.getInt("status");
                        runOnUiThread(() -> {
                            Toast.makeText(Login.this, error, Toast.LENGTH_LONG).show();
                            if (status == 400) {
                                clearUserData(); // Clear user data on incorrect password
                            }
                        });
                        return; // Exit early if there's an error
                    }

                    if (!jsonObject.has("result") || !jsonObject.has("token")) {
                        runOnUiThread(() -> Toast.makeText(Login.this, "Invalid response from server", Toast.LENGTH_LONG).show());
                        return; // Exit early if result or token is missing
                    }

                    JSONObject resultObject = jsonObject.getJSONObject("result");
                    JSONObject userObject = resultObject.getJSONObject("user");
                    String token = jsonObject.getString("token");

                    processUserLogin(userObject, email, password, token);

                } catch (JSONException | IOException e) {
                    Log.e("Exception", "Error processing response", e);
                    runOnUiThread(() -> Toast.makeText(Login.this, "An error occurred while processing your request. Please try again.", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply(); // Clearing preferences
    }

    private void processUserLogin(JSONObject userObject, String email, String password, String token) {
        runOnUiThread(() -> {
            try {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (binding.RememberMe.isChecked()) {
                    editor.putString("email", email);
                    editor.putString("password", password);
                } else {
                    editor.clear();
                }
                editor.apply();

                session.createLoginSession(
                        userObject.getString("id"),
                        userObject.getString("username"),
                        userObject.getString("email"),
                        userObject.getString("first_name"),
                        userObject.getString("last_name"),
                        userObject.getString("phone"),
                        token,
                        userObject.getString("gender"),
                        userObject.getString("group_id"),
                        userObject.optString("warehouse_id", ""),
                        userObject.getString("biller_id"),
                        userObject.optString("company_id", ""),
                        userObject.getString("show_cost"),
                        userObject.getString("show_price")
//                        userObject.getString("award_points")
                );

                Intent intent;
//                if (userObject.has("group_id") && userObject.getString("group_id").equals("13"))
                if (userObject.has("group_id") && userObject.getString("group_id").equals("1"))
                {
                    intent = new Intent(Login.this, MainActivity.class);
                } else {
                    intent = new Intent(Login.this, PeopleActivity.class);
                }
                startActivity(intent);
                finish(); // Finish current activity after successful login
            } catch (JSONException e) {
                Log.e("JSONException", "Error parsing user object: " + e.getMessage());
                Toast.makeText(Login.this, "Error parsing user data", Toast.LENGTH_LONG).show();
            }
        });
    }


    public boolean validate() {
        boolean valid = true;
        if (binding.LoginName.getEditText().getText().toString().trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.LoginName.getEditText().getText().toString().trim()).matches()) {
            binding.LoginName.setError("Please Enter valid Email Address");
            valid = false;
        } else {
            binding.LoginName.setError("");
        }
        if (binding.LoginPassword.getEditText().getText().toString().trim().isEmpty()) {
            binding.LoginPassword.setError("Please Enter Password");
            valid = false;
        } else {
            binding.LoginPassword.setError("");
        }
        return valid;
    }
}


