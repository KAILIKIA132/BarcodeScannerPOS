package com.arodi.powergas.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arodi.powergas.R;
import com.arodi.powergas.models.PaymentModel;
import com.arodi.powergas.models.StoreModel;
import com.arodi.powergas.models.TodayModel;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<StoreModel> storeModel;
    Context context;
    JSONArray jsonArray = new JSONArray();


    public StoreAdapter(Context context, ArrayList<StoreModel> storeModel) {
        this.storeModel = storeModel;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StoreAdapter.StoreViewHolder(LayoutInflater.from(context).inflate(R.layout.store_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((StoreAdapter.StoreViewHolder) viewHolder, position);

    }


    @Override
    public int getItemCount() {
        return storeModel.size();
    }


    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView name, product_id;
        TextInputLayout textInputLayout;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.Name);
            product_id = itemView.findViewById(R.id.ProductId);
            textInputLayout = itemView.findViewById(R.id.Qty);
        }
    }


    private void populateItemRows(StoreAdapter.StoreViewHolder holder, int position) {
        StoreModel model = storeModel.get(position);


        holder.name.setText(model.getProduct_name());
        holder.product_id.setText(model.getProduct_id());

        holder.textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("product_id", model.getProduct_id());
                    jsonObject.put("product_name", model.getProduct_name());
                    jsonObject.put("product_quantity", s.toString().trim());
                    jsonObject.put("product_price", model.getProduct_price());
                    jsonArray.put(position, jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    public JSONArray getStock() {
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (object.has("product_quantity")) {
                    if (!object.getString("product_quantity").equals("")){
                        array.put(object);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }


}
