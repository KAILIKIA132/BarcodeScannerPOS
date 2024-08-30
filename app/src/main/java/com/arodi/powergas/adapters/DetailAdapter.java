package com.arodi.powergas.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arodi.powergas.R;
import com.arodi.powergas.database.Database;
import com.arodi.powergas.models.ProductHistory;
import com.arodi.powergas.models.SaleModel;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<ProductHistory> productHistory;
    Context context;


    public DetailAdapter(Context context, ArrayList<ProductHistory> productHistory) {
        this.productHistory = productHistory;
        this.context = context;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DetailAdapter.DetailViewHolder(LayoutInflater.from(context).inflate(R.layout.detail_raw, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((DetailAdapter.DetailViewHolder) viewHolder, position);
        
    }
    
    
    
    @Override
    public int getItemCount() {
        return productHistory.size();
    }
    
    
    
    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView id, name, price, qty, total;
        ImageView remove;
        
        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.Name);
            id = itemView.findViewById(R.id.Id);
            price = itemView.findViewById(R.id.Price);
            qty = itemView.findViewById(R.id.Qty);
            total = itemView.findViewById(R.id.Total);
            remove = itemView.findViewById(R.id.Remove);
        }
    }
   
    
    private void populateItemRows(DetailAdapter.DetailViewHolder holder, int position) {
        ProductHistory model = productHistory.get(position);
        
        holder.name.setText(model.getName());
        holder.id.setText(model.getProduct_id());
        holder.price.setText(model.getPrice());
        holder.qty.setText(model.getQuantity());
        holder.total.setText(String.valueOf(Integer.parseInt(model.getPrice()) * Integer.parseInt(model.getQuantity())));

    }
    
    
    
}
