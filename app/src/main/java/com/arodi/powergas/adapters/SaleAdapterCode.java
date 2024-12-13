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
import com.arodi.powergas.interfaces.shopInterface;
import com.arodi.powergas.models.SaleModelCode;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SaleAdapterCode extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<SaleModelCode> saleModel;
    private Context context;
    private shopInterface shop;

    public SaleAdapterCode(Context context, ArrayList<SaleModelCode> saleModel, shopInterface shop) {
        this.saleModel = saleModel;
        this.context = context;
        this.shop = shop;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomerViewHolder(LayoutInflater.from(context).inflate(R.layout.sale_raw, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((CustomerViewHolder) viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return saleModel.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView name, discount, price, qty, total;
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
    private void populateItemRows(CustomerViewHolder holder, int position) {
        try {
            SaleModelCode model = saleModel.get(position);

            // Bind data safely with null checks
            holder.name.setText(model.getProductName() != null ? model.getProductName() : "N/A");
            holder.discount.setText(model.getDiscount() != null ? model.getDiscount() : "0");
            holder.price.setText(model.getProductPrice() != null ? model.getProductPrice() : "0");
            holder.qty.setText(model.getProductSales() != null ? model.getProductSales() : "0");
            holder.total.setText(model.getTotal() != null ? model.getTotal() : "0");

            // Remove functionality with edge case handling
            holder.remove.setOnClickListener(view -> {
                final SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
                dialog.setTitle(context.getString(R.string.remove_product_title));
                dialog.setConfirmText(context.getString(R.string.remove_button_text));
                dialog.setConfirmClickListener(sweetAlertDialog -> {
                    saleModel.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, saleModel.size());  // Update the indices of the remaining items
                    shop.shopData();  // Refresh the shop data
                    sweetAlertDialog.dismiss();
                });
                dialog.show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    private void populateItemRows(CustomerViewHolder holder, int position) {
//        try {
//            SaleModelCode model = saleModel.get(position);
//
//            // Bind the data from the SaleModelCode to the view holder
//            holder.name.setText(model.getProductName());
//            holder.discount.setText(model.getDiscount());
//            holder.price.setText(model.getProductPrice());
//            holder.qty.setText(model.getProductSales());
//            holder.total.setText(model.getTotal());
//
//            // Handle the removal of the sale item
//            holder.remove.setOnClickListener(view -> {
//                final SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
//                dialog.setTitle("You are about to remove a product!");
//                dialog.setConfirmText("Remove");
//                dialog.setConfirmClickListener(sweetAlertDialog -> {
//                    saleModel.remove(position);  // Remove item from the list
//                    notifyItemRemoved(position);  // Notify the adapter that the item has been removed
//                    shop.shopData();  // Refresh the shop data
//                    sweetAlertDialog.dismiss();
//                });
//                dialog.show();
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
