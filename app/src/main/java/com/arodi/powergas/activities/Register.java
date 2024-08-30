package com.arodi.powergas.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.arodi.powergas.R;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.databinding.ActivityRegisterBinding;
import com.arodi.powergas.models.CategoryModel;
import com.arodi.powergas.models.CityModel;
import com.arodi.powergas.models.DistributorModel;
import com.arodi.powergas.models.NewCustomerModel;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

public class Register extends AppCompatActivity {
    ActivityRegisterBinding binding;
    SweetAlertDialog sweetAlertDialog;

    public String selected_distributor_id;
    ArrayList<DistributorModel> distributorModel = new ArrayList<>();
    ArrayList<String> distributorList = new ArrayList<>();

    Dialog dialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);

        fetchDistributor();
        binding.RegisterButton.setOnClickListener(v -> {
            if(validate()) {
                register();
            }
        });


        binding.Distributor.setOnClickListener(v -> {
            dialog = new Dialog(Register.this, R.style.DialogTheme);
            if (!dialog.isShowing()) {

                dialog.setContentView(R.layout.dialog_spinner);

                dialog.show();

                TextInputLayout textInputLayout = dialog.findViewById(R.id.Search);
                ListView listView = dialog.findViewById(R.id.ItemList);

                TextView close = dialog.findViewById(R.id.Close);
                close.setOnClickListener(v1 -> dialog.dismiss());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(Register.this, android.R.layout.simple_list_item_1, distributorList);

                listView.setAdapter(adapter);

                textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    binding.Distributor.setText(adapter.getItem(position));

                    DistributorModel model = distributorModel.get(position);
                    selected_distributor_id = model.getId();

                    dialog.dismiss();
                });
            }
        });



    }

    private void fetchDistributor() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "customers?action=fetch_distributor")
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
                        JSONArray jsonArray = new JSONArray(response.body().string());

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            DistributorModel model = new DistributorModel(
                                    jsonObject.getString("id"),
                                    jsonObject.getString("name"));
                            distributorModel.add(model);
                        }

                        for (int i = 0; i < distributorModel.size(); i++) {
                            String name = distributorModel.get(i).getName();
                            distributorList.add(name);
                        }
                        binding.LoadDistributor.setVisibility(View.GONE);

                        if (distributorList.size() == 0) {
                            binding.LoadDistributor.setVisibility(View.VISIBLE);
                            binding.LoadDistributor.setText("No distributor available!");
                        } else {
                            binding.LoadDistributor.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        Toast.makeText(Register.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }
    
    public void register(){
        sweetAlertDialog = new SweetAlertDialog(Register.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitle("Please Wait...");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("distributor_id", selected_distributor_id)
                .addFormDataPart("first_name", binding.FirstName.getEditText().getText().toString().trim())
                .addFormDataPart("last_name", binding.LastName.getEditText().getText().toString().trim())
                .addFormDataPart("email", binding.RegisterEmail.getEditText().getText().toString().trim())
                .addFormDataPart("phone", binding.RegisterPhone.getEditText().getText().toString().trim())
                .addFormDataPart("password", binding.RegisterPassword.getEditText().getText().toString().trim())
                .addFormDataPart("password_confirm", binding.RegisterPassword.getEditText().getText().toString().trim())
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.ROUTE_URL+"register")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    sweetAlertDialog.dismiss();
                    Toast.makeText(Register.this, "No Connection to host", Toast.LENGTH_SHORT).show();
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
                            
                            sweetAlertDialog.dismiss();
                            Intent intent = new Intent(Register.this, Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }else {
                            sweetAlertDialog.dismiss();
                            Toast.makeText(Register.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                        
                    } catch (Exception e) {
                        sweetAlertDialog.dismiss();
                        Toast.makeText(Register.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    
                    
                });
                
            }
        });
        
        
    }
    
    public boolean validate(){
        boolean valid = true;
        if(binding.FirstName.getEditText().getText().toString().trim().isEmpty()){
            binding.FirstName.setError("Please Enter First Name");
            valid = false;
        }else {
            binding.FirstName.setError("");
        }

        if(binding.LastName.getEditText().getText().toString().trim().isEmpty()){
            binding.LastName.setError("Please Enter Last Name");
            valid = false;
        }else {
            binding.LastName.setError("");
        }

        if(binding.RegisterEmail.getEditText().getText().toString().trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.RegisterEmail.getEditText().getText().toString().trim()).matches()){
            binding.RegisterEmail.setError("Please Enter valid Email Address");
            valid = false;
        }else {
            binding.RegisterEmail.setError("");
        }
        if(binding.RegisterPhone.getEditText().getText().toString().trim().isEmpty()){
            binding.RegisterPhone.setError("Please Enter Phone");
            valid = false;
        }else {
            binding.RegisterPhone.setError("");
        }
        if(binding.RegisterPassword.getEditText().getText().toString().trim().isEmpty() || binding.RegisterPassword.getEditText().getText().toString().trim().length()<8 || binding.RegisterPassword.getEditText().getText().toString().trim().length()>25){
            binding.RegisterPassword.setError("Please enter alpha-numeric characters between 8 and 25");
            valid = false;
        }else {
            binding.RegisterPassword.setError("");
        }
        
        if(binding.RegisterConfirm.getEditText().getText().toString().trim().isEmpty()){
            binding.RegisterConfirm.setError("Please confirm password");
            valid = false;
        }else {
            binding.RegisterConfirm.setError("");
        }
        
        if(!binding.RegisterConfirm.getEditText().getText().toString().trim().equals(binding.RegisterPassword.getEditText().getText().toString().trim())){
            binding.RegisterConfirm.setError("The password do not match");
            valid = false;
        }else {
            binding.RegisterConfirm.setError("");
        }
        
        return valid;
    }
    
}
