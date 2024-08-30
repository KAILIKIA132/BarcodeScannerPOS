package com.arodi.powergas.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.arodi.powergas.activities.InvoiceDetail;
import com.arodi.powergas.entities.InvoiceEntity;
import com.arodi.powergas.models.HistoryModel;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    public List<InvoiceEntity> invoiceEntity;

    public InvoiceAdapter(Context context2, List<InvoiceEntity> invoiceEntity) {
        this.invoiceEntity = invoiceEntity;
        this.context = context2;
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DiscountViewHolder(LayoutInflater.from(context).inflate(R.layout.customer_raw, viewGroup, false));
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        populateItemRows((DiscountViewHolder) viewHolder, i);
    }

    public int getItemCount() {
        List<InvoiceEntity> arrayList = this.invoiceEntity;
        if (arrayList == null) {
            return 0;
        }
        return arrayList.size();
    }

    public static class DiscountViewHolder extends RecyclerView.ViewHolder {
        TextView group;
        ImageView imageView;
        MaterialButton next;
        TextView phone;
        TextView shop;
        TextView town;

        public DiscountViewHolder(View view) {
            super(view);
            shop = view.findViewById(R.id.Shop);
            town = view.findViewById(R.id.Town);
            group = view.findViewById(R.id.Group);
            phone = view.findViewById(R.id.Phone);
            next = view.findViewById(R.id.Next);
            imageView = view.findViewById(R.id.Photo);
        }
    }

    private void populateItemRows(DiscountViewHolder viewHolder, int i) {
        InvoiceEntity entity = this.invoiceEntity.get(i);
        String str = entity.getPayment_status().equals("0") ? "Pending" : "Approved";
        viewHolder.shop.setText(entity.getShop_name());

        viewHolder.group.setText(str + " - " + entity.getGrand_total() + "/=");
        viewHolder.town.setText(entity.getUpdated_at());
        viewHolder.phone.setText(entity.getPhone());
        Picasso.with(context).load(entity.getImage()).into(viewHolder.imageView);

        viewHolder.next.setOnClickListener(v ->  {
            if (entity.getPayment_status().equals("1")) {
                Intent intent = new Intent(context, InvoiceDetail.class);
                intent.putExtra("InvoiceInformation", entity);
                context.startActivity(intent);
            }else {
                Toast.makeText(this.context, "Please contact system admin for approval", Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.phone.setOnClickListener(v->{
            Intent intent = new Intent("android.intent.action.DIAL");
            intent.setData(Uri.parse("tel:" + entity.getPhone()));
            this.context.startActivity(intent);
        });
    }


}
