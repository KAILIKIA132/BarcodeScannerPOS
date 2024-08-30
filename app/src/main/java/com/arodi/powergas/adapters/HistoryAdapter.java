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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arodi.powergas.R;
import com.arodi.powergas.activities.HistoryDetail;
import com.arodi.powergas.activities.ShopActivity;
import com.arodi.powergas.models.CustomerModel;
import com.arodi.powergas.models.HistoryModel;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<HistoryModel> historyModel;
    Context context;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;


    public HistoryAdapter(Context context, ArrayList<HistoryModel> historyModel) {
        this.historyModel = historyModel;
        this.context = context;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new HistoryAdapter.HistoryViewHolder(LayoutInflater.from(context).inflate(R.layout.customer_raw, parent, false));
        } else {
            return new HistoryAdapter.LoadingViewHolder(LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false));
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        
        if (viewHolder instanceof HistoryAdapter.HistoryViewHolder) {
            populateItemRows((HistoryAdapter.HistoryViewHolder) viewHolder, position);
        } else if (viewHolder instanceof HistoryAdapter.LoadingViewHolder) {
            showLoadingView((HistoryAdapter.LoadingViewHolder) viewHolder, position);
        }
        
    }
    
    
    
    @Override
    public int getItemCount() {
        return historyModel == null ? 0 : historyModel.size();
    }
    
    @Override
    public int getItemViewType(int position) {
        return historyModel.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }
    
    
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView shop, town, group, phone;
        MaterialButton next;
        ImageView imageView;
        
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            shop = itemView.findViewById(R.id.Shop);
            town = itemView.findViewById(R.id.Town);
            group = itemView.findViewById(R.id.Group);
            phone = itemView.findViewById(R.id.Phone);
            next = itemView.findViewById(R.id.Next);
            imageView = itemView.findViewById(R.id.Photo);
        }
    }
    
    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        
        ProgressBar progressBar;
        
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
    
    private void showLoadingView(HistoryAdapter.LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
        
    }
    
    private void populateItemRows(HistoryAdapter.HistoryViewHolder holder, int position) {
        HistoryModel model = historyModel.get(position);
        holder.shop.setText(model.getShop_name());
        holder.group.setText(model.getPayment_status()+" - "+model.getGrand_total()+"/=");
        holder.town.setText(model.getUpdated_at());
        holder.phone.setText(model.getPhone());

        Picasso.with(context).load(model.getImage()).into(holder.imageView);
        
        holder.next.setOnClickListener(view -> {
            Intent intent = new Intent(context, HistoryDetail.class);
            intent.putExtra("HistoryInformation", model);

            SharedPreferences sharedPreferences = context.getSharedPreferences("CUSTOMER_SHOPPING_DATA", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("SAVED_INSTANCE", "HISTORY_LIST");
            editor.apply();

            context.startActivity(intent);
        });

        holder.phone.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + model.getPhone()));
            context.startActivity(callIntent);
        });
        
    }
    
    
    
}
