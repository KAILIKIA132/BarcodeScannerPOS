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
import com.arodi.powergas.interfaces.shopInterface;
import com.arodi.powergas.models.SaleModel;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SaleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    public ArrayList<SaleModel> saleModel;
    Context context;
    shopInterface shop;
    
    
    public SaleAdapter(Context context, ArrayList<SaleModel> saleModel, shopInterface shop) {
        this.saleModel = saleModel;
        this.context = context;
        this.shop = shop;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SaleAdapter.CustomerViewHolder(LayoutInflater.from(context).inflate(R.layout.sale_raw, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((SaleAdapter.CustomerViewHolder) viewHolder, position);
        
    }
    
    
    
    @Override
    public int getItemCount() {
        return saleModel.size();
    }
    
    
    
    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView discount, name, price, qty, total;
        ImageView remove;
        
        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.Name);
            discount = itemView.findViewById(R.id.Discount);
            price = itemView.findViewById(R.id.Price);
            qty = itemView.findViewById(R.id.Qty);
            total = itemView.findViewById(R.id.Total);
            remove = itemView.findViewById(R.id.Remove);
        }
    }
   
    
    private void populateItemRows(SaleAdapter.CustomerViewHolder holder, int position) {
        try {
            SaleModel model = saleModel.get(position);

            holder.name.setText(model.getName());
            holder.discount.setText(model.getDiscount());
            holder.price.setText(model.getPrice());
            holder.qty.setText(model.getQuantity());
            holder.total.setText(model.getTotal());

            holder.remove.setOnClickListener(view -> {
                final SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
                dialog.setTitle("You are about to remove a product!");
                dialog.setConfirmText("Remove");
                dialog.setConfirmClickListener(sweetAlertDialog -> {
                    new Database(context).clear_single_sale(model.getProduct_id());
                    shop.shopData();
                    sweetAlertDialog.dismiss();
                });
                dialog.show();
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    
}
