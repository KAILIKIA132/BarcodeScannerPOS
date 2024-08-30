package com.arodi.powergas.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arodi.powergas.R;
import com.arodi.powergas.activities.ChequeDetail;
import com.arodi.powergas.activities.DiscountDetail;
import com.arodi.powergas.entities.ChequeEntity;
import com.arodi.powergas.models.HistoryModel;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChequeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<ChequeEntity> chequeEntity;
    Context context;


    public ChequeAdapter(Context context, List<ChequeEntity> chequeEntity) {
        this.chequeEntity = chequeEntity;
        this.context = context;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChequeAdapter.ChequeViewHolder(LayoutInflater.from(context).inflate(R.layout.customer_raw, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((ChequeAdapter.ChequeViewHolder) viewHolder, position);
    }
    
    
    
    @Override
    public int getItemCount() {
        return chequeEntity == null ? 0 : chequeEntity.size();
    }

    public static class ChequeViewHolder extends RecyclerView.ViewHolder {
        TextView shop, town, group, phone;
        MaterialButton next;
        ImageView imageView;
        
        public ChequeViewHolder(@NonNull View itemView) {
            super(itemView);
            shop = itemView.findViewById(R.id.Shop);
            town = itemView.findViewById(R.id.Town);
            group = itemView.findViewById(R.id.Group);
            phone = itemView.findViewById(R.id.Phone);
            next = itemView.findViewById(R.id.Next);
            imageView = itemView.findViewById(R.id.Photo);
        }
    }
    
    private void populateItemRows(ChequeAdapter.ChequeViewHolder holder, int position) {
        ChequeEntity model = chequeEntity.get(position);

        String status = model.getPayment_status().equals("0") ? "Pending" : "Approved";
        holder.shop.setText(model.getShop_name());
        holder.group.setText(status+" - "+model.getGrand_total()+"/=");
        holder.town.setText(model.getUpdated_at());
        holder.phone.setText(model.getPhone());

        Picasso.with(context).load(model.getImage()).into(holder.imageView);
        
        holder.next.setOnClickListener(view -> {
            if (model.getPayment_status().equals("1")) {
                Intent intent = new Intent(context, ChequeDetail.class);
                intent.putExtra("ChequeInformation", model);

                SharedPreferences sharedPreferences = context.getSharedPreferences("CUSTOMER_SHOPPING_DATA", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("SAVED_INSTANCE", "CHEQUE_LIST");
                editor.apply();

                context.startActivity(intent);
            }else {
                Toast.makeText(context, "Please contact system admin for approval", Toast.LENGTH_SHORT).show();
            }
        });

        holder.phone.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + model.getPhone()));
            context.startActivity(callIntent);
        });
        
    }
    
    
    
}
