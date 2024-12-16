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
import com.arodi.powergas.activities.ShopActivityThree;
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
            if (position >= 0) {
                // Save customer details to SharedPreferences
                SharedPreferences sharedPreferences = context.getSharedPreferences("CustomerPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("CUSTOMER_ID", model.getCustomer_id());
                editor.putString("LAT", model.getLat());
                editor.putString("LNG", model.getLng());
                editor.putString("TOWN_ID", model.getTown_id());
                editor.putString("SHOP_ID", model.getShop_id());
                editor.putString("CUSTOMER_EMAIL", model.getEmail());
                editor.putString("CUSTOMER_PHONE", model.getPhone());
                editor.putString("SELECTED_CUSTOMER_NAME", model.getName());
                editor.putString("SELECTED_CUSTOMER_GROUP_NAME", model.getCustomer_group_name());
                editor.putString("SELECTED_CUSTOMER_COUNTY_NAME", model.getCounty_name());
                editor.putString("SELECTED_CUSTOMER_GROUP_ID", model.getCustomer_group_id());
                editor.apply(); // Apply changes to SharedPreferences


                Intent intent = new Intent(context, ShopActivity.class);
                // Put customer data in the intent as extras
                intent.putExtra("CUSTOMER_ID", model.getCustomer_id());
                intent.putExtra("LAT", model.getLat());
                intent.putExtra("LNG", model.getLng());
                intent.putExtra("TOWN_ID", model.getTown_id());
                intent.putExtra("SHOP_ID", model.getShop_id());
                intent.putExtra("CUSTOMER_EMAIL", model.getEmail());
                intent.putExtra("CUSTOMER_PHONE", model.getPhone());
                intent.putExtra("SELECTED_CUSTOMER_NAME", model.getName());
                intent.putExtra("SELECTED_CUSTOMER_GROUP_NAME", model.getCustomer_group_name());
                intent.putExtra("SELECTED_CUSTOMER_COUNTY_NAME", model.getCounty_name());
                intent.putExtra("SELECTED_CUSTOMER_GROUP_ID", model.getCustomer_group_id());




                // Log the data for debugging
                Log.d("IntentExtras", "CUSTOMER_ID: " + model.getCustomer_id());
                Log.d("IntentExtras", "CUSTOMER_NAME: " + model.getName());
                Log.d("IntentExtras", "LAT: " + model.getLat());
                Log.d("IntentExtras", "LNG: " + model.getLng());
                Log.d("IntentExtras", "TOWN_ID: " + model.getTown_id());
                Log.d("IntentExtras", "SHOP_ID: " + model.getShop_id());
                Log.d("IntentExtras", "CUSTOMER_PHONE: " + model.getPhone());
                Log.d("IntentExtras", "SELECTED_CUSTOMER_GROUP_NAME: " + model.getCustomer_group_name());
                Log.d("IntentExtras", "SELECTED_CUSTOMER_COUNTY_NAME: " + model.getCounty_name());
                Log.d("IntentExtras", "SELECTED_CUSTOMER_GROUP_ID: " + model.getCustomer_group_id());
                // Start the next activity
                context.startActivity(intent);
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
