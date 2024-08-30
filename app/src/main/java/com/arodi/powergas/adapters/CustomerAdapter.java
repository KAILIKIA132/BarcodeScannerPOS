package com.arodi.powergas.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.arodi.powergas.activities.ShopActivity;
import com.arodi.powergas.activities.ShopActivityTwo;
import com.arodi.powergas.models.CustomerModel;
import com.arodi.powergas.R;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    public ArrayList<CustomerModel> arrayList;

    public CustomerAdapter(Context context, ArrayList<CustomerModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomerAdapter.CustomerViewHolder(LayoutInflater.from(context).inflate(R.layout.customer_raw, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((CustomerAdapter.CustomerViewHolder) viewHolder, position);
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView shop, town, group, phone;
        MaterialButton next;
        ImageView imageView;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            shop = itemView.findViewById(R.id.Shop);
            town = itemView.findViewById(R.id.Town);
            group = itemView.findViewById(R.id.Group);
            phone = itemView.findViewById(R.id.Phone);
            next = itemView.findViewById(R.id.Next);
            imageView = itemView.findViewById(R.id.Photo);

        }
    }


    private void populateItemRows(CustomerAdapter.CustomerViewHolder holder, int position) {
        CustomerModel model = arrayList.get(position);

        holder.shop.setText(model.getShop_name() + " (" + model.getDistance() + " " + "KM)");
        holder.town.setText(model.getTown_name());
        holder.group.setText(model.getCustomer_group_name());
        holder.phone.setText(model.getPhone());

        Picasso.with(context).load(model.getLogo()).into(holder.imageView);

        holder.next.setOnClickListener(view -> {
            if (position == 0) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("CUSTOMER_SHOPPING_DATA", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("CUSTOMER_ID", model.getCustomer_id());
                editor.putString("CUSTOMER_NAME", model.getShop_name());
                editor.putString("LAT", model.getLat());
                editor.putString("LNG", model.getLng());
                editor.putString("TOWN_ID", model.getTown_id());
                editor.putString("SHOP_ID", model.getShop_id());
                editor.putString("SAVED_INSTANCE", "CUSTOMER_LIST");
                editor.putString("CUSTOMER_PHONE", model.getPhone());
                editor.putString("SELECTED_CUSTOMER_NAME", model.getName());
                editor.apply();



                // Log the data
                Log.d("SharedPreferences", "CUSTOMER_ID: " + model.getCustomer_id());
                Log.d("SharedPreferences", "CUSTOMER_NAME: " + model.getShop_name());
                Log.d("SharedPreferences", "LAT: " + model.getLat());
                Log.d("SharedPreferences", "LNG: " + model.getLng());
                Log.d("SharedPreferences", "TOWN_ID: " + model.getTown_id());
                Log.d("SharedPreferences", "SHOP_ID: " + model.getShop_id());
                Log.d("SharedPreferences", "SAVED_INSTANCE: CUSTOMER_LIST");
                Log.d("SharedPreferences", "CUSTOMER_PHONE: " + model.getPhone());
                Log.d("SharedPreferences", "SELECTED_CUSTOMER_NAME: " + model.getName());


                context.startActivity(new Intent(context, ShopActivity.class));
            } else {
                Toast.makeText(context, "Please attend to the first customer first.", Toast.LENGTH_LONG).show();
            }
        });


        holder.phone.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + model.getPhone()));
            context.startActivity(callIntent);
        });

    }


}
