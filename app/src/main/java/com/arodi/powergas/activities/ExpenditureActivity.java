package com.arodi.powergas.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.arodi.powergas.R;
import com.arodi.powergas.adapters.ExpenditureAdapter;
import com.arodi.powergas.api.BaseURL;
import com.arodi.powergas.background.ExpenseWorker;
import com.arodi.powergas.background.HistoryWorker;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.databinding.ActivityExpenditureBinding;
import com.arodi.powergas.databinding.ExpenseDialogBinding;
import com.arodi.powergas.helpers.NetworkState;
import com.arodi.powergas.models.ExpenditureModel;
import com.arodi.powergas.session.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExpenditureActivity extends AppCompatActivity {

    ActivityExpenditureBinding binding;
    ExpenditureAdapter adapter;
    ArrayList<ExpenditureModel> expenditureModel = new ArrayList<>();
    public boolean isLoading = false;
    SessionManager sessionManager;
    HashMap<String, String> user;
    ExpenseDialogBinding dialogBinding;
    String selected_expense;
    Bitmap bitmap;


    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_expenditure);
        sessionManager = new SessionManager(ExpenditureActivity.this);
        user = new SessionManager(ExpenditureActivity.this).getLoginDetails();

        binding.Refresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.BLUE, Color.GREEN);
        binding.Refresh.setOnRefreshListener(
                this::getExpenditure
        );

        binding.back.setOnClickListener(view -> onBackPressed());
        binding.AddExpenditure.setOnClickListener(view -> showDialog());

        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(ExpenditureActivity.this));

        populateExpenditure();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkState.getInstance(ExpenditureActivity.this).isConnected()) {
            getExpenditure();
        }
    }

    private void initAdapter() {
        adapter = new ExpenditureAdapter(ExpenditureActivity.this, expenditureModel);
        adapter.notifyDataSetChanged();
        binding.recyclerview.setAdapter(adapter);

        if (expenditureModel.size() == 0) {
            binding.RelNo.setVisibility(View.VISIBLE);
        } else {
            binding.RelNo.setVisibility(View.GONE);
        }
    }

    private void initScrollListener() {
        binding.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == expenditureModel.size() - 1) {
                        //bottom of list!
                        if (dy > 0) {
                            loadMore();
                            isLoading = true;
                        }
                    }
                }
            }
        });


    }

    private void loadMore() {
        if (expenditureModel.size() != 0) {
            expenditureModel.add(null);
            binding.recyclerview.post(() -> adapter.notifyItemInserted(expenditureModel.size() - 1));

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (expenditureModel.size() != 0) {
                    expenditureModel.remove(expenditureModel.size() - 1);
                    int scrollPosition = expenditureModel.size();
                    adapter.notifyItemRemoved(scrollPosition);
                    System.out.println("scrollPosition" + scrollPosition);
                    try {
                        JSONArray jsonArray = new JSONArray(new Gson().toJson(new Database(getBaseContext()).fetch_expense(scrollPosition)));

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            ExpenditureModel model = new ExpenditureModel(
                                    jsonObject.getString("expense_id"),
                                    jsonObject.getString("date"),
                                    jsonObject.getString("company_id"),
                                    jsonObject.getString("reference"),
                                    jsonObject.getString("amount"),
                                    jsonObject.getString("approved"),
                                    jsonObject.getString("note"),
                                    jsonObject.getString("status"));
                            expenditureModel.add(model);

                            adapter.notifyItemInserted(i);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    adapter.notifyDataSetChanged();
                    isLoading = false;
                }
            }, 2000);

        }
    }

    public void showDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ExpenditureActivity.this, R.style.CustomBottomSheetDialogTheme);
        dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(ExpenditureActivity.this), R.layout.expense_dialog, null, false);
        bottomSheetDialog.setContentView(dialogBinding.getRoot());

        dialogBinding.cancel.setOnClickListener(view -> bottomSheetDialog.dismiss());

        List<String> list = new ArrayList<>();
        list.add("Lunch");
        list.add("Airtime");
        list.add("Maintenance");
        list.add("Parking");
        list.add("Discount");
        list.add("Accommodation");
        list.add("Casual");

        dialogBinding.Expense.setOnClickListener(v -> {
            dialog = new Dialog(ExpenditureActivity.this, R.style.DialogTheme);

            dialog.setContentView(R.layout.dialog_spinner);

            dialog.show();

            TextInputLayout textInputLayout = dialog.findViewById(R.id.Search);
            ListView listView = dialog.findViewById(R.id.ItemList);

            TextView close = dialog.findViewById(R.id.Close);
            close.setOnClickListener(v1 -> dialog.dismiss());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(ExpenditureActivity.this, android.R.layout.simple_list_item_1, list);

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
                dialogBinding.Expense.setText(adapter.getItem(position));
                selected_expense = adapter.getItem(position);

                dialog.dismiss();
            });
        });

        dialogBinding.Gallery.setOnClickListener(view -> {
            Toast.makeText(ExpenditureActivity.this, "Not available please use camera", Toast.LENGTH_SHORT).show();
        });

        dialogBinding.Camera.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(ExpenditureActivity.this.getPackageManager()) != null) {
                startActivityForResult(intent, 480);
            }
        });

        dialogBinding.confirmButton.setOnClickListener(view -> {
            if (validate()) {
                createExpense();
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (dialogBinding == null) {
            dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(ExpenditureActivity.this), R.layout.expense_dialog, null, false);
        }

        if (requestCode == 480 && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            dialogBinding.Image.setImageBitmap(bitmap);
        }


    }

    public String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByteArray, Base64.DEFAULT);
    }

    public void createExpense() {
        final SweetAlertDialog dialog = new SweetAlertDialog(ExpenditureActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitle("Please Wait...");
        dialog.setCancelable(false);
        dialog.show();

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("company_id", user.get(SessionManager.KEY_SALESMAN_ID));
            jsonObject.put("vehicle_id", user.get(SessionManager.KEY_VEHICLE_ID));
            jsonObject.put("distributor_id", user.get(SessionManager.KEY_DISTRIBUTOR_ID));
            jsonObject.put("salesman_id", user.get(SessionManager.KEY_SALESMAN_ID));
            jsonObject.put("reference", selected_expense);
            jsonObject.put("amount", dialogBinding.Amount.getEditText().getText().toString().trim());
            jsonObject.put("note", dialogBinding.Note.getEditText().getText().toString().trim());
            jsonObject.put("created_by", user.get(SessionManager.KEY_USERNAME));
            jsonObject.put("image", getStringImage(bitmap));
            jsonArray.put(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonArray.toString());

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "expenses/?action=create_expense")
                .method("POST", body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("errorrr" + e.toString());
                    dialog.dismiss();
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    try {
                        JSONObject object = new JSONObject(response.body().string());

                        if (object.getString("success").equals("1")) {
                            Toast.makeText(ExpenditureActivity.this, object.getString("message"), Toast.LENGTH_SHORT).show();
                            getExpenditure();
                        } else {
                            Toast.makeText(ExpenditureActivity.this, object.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(ExpenditureActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


                });

            }
        });
    }

    public boolean validate() {
        boolean valid = true;

        if (bitmap == null) {
            Toast.makeText(ExpenditureActivity.this, "Please Capture Receipt Image", Toast.LENGTH_SHORT).show();
            valid = false;
        }


        if (selected_expense == null) {
            Toast.makeText(ExpenditureActivity.this, "Please select expense", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (dialogBinding.Amount.getEditText().getText().toString().trim().equals("")) {
            dialogBinding.Amount.setError("Please input amount");
            valid = false;
        } else {
            dialogBinding.Amount.setError("");
        }

        if (dialogBinding.Note.getEditText().getText().toString().trim().equals("")) {
            dialogBinding.Note.setError("Please input note");
            valid = false;
        } else {
            dialogBinding.Note.setError("");
        }
        return valid;
    }

    public void populateExpenditure() {
        expenditureModel.clear();
        expenditureModel = (ArrayList<ExpenditureModel>) new Database(getBaseContext()).fetch_expense(0);
        initAdapter();
        initScrollListener();
    }

    private void getExpenditure() {
        binding.Refresh.setRefreshing(true);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BaseURL.SERVER_URL + "expenses?action=fetch_expenses&salesman_id=" + user.get(SessionManager.KEY_SALESMAN_ID))
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("errorrr" + e.toString());
                    binding.Refresh.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences(ExpenseWorker.SHARED_PREFERENCE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(ExpenseWorker.JSON_ARRAY, response.body().string());
                    editor.apply();

                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ExpenseWorker.class).build();
                    WorkManager.getInstance().enqueue(workRequest);

                    runOnUiThread(() -> {
                        if (ExpenditureActivity.this != null) {
                            if (workRequest != null) {
                                WorkManager.getInstance().getWorkInfoByIdLiveData(workRequest.getId()).observe(ExpenditureActivity.this, workInfo -> {
                                    if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                        populateExpenditure();
                                        binding.Refresh.setRefreshing(false);
                                    }
                                });
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
}