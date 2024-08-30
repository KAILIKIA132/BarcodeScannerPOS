package com.arodi.powergas.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arodi.powergas.R;
import com.arodi.powergas.models.StockModel;
import com.arodi.powergas.models.TodayModel;

import java.util.ArrayList;

public class TodayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<TodayModel> todayModel;
    Context context;


    public TodayAdapter(Context context, ArrayList<TodayModel> todayModel) {
        this.todayModel = todayModel;
        this.context = context;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TodayAdapter.TodayViewHolder(LayoutInflater.from(context).inflate(R.layout.today_row, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((TodayAdapter.TodayViewHolder) viewHolder, position);
        
    }
    
    
    
    @Override
    public int getItemCount() {
        return todayModel.size();
    }
    
    
    
    public static class TodayViewHolder extends RecyclerView.ViewHolder {
        TextView id,name,qty,total;
        
        public TodayViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.Id);
            name = itemView.findViewById(R.id.Name);
            qty = itemView.findViewById(R.id.Qty);
            total = itemView.findViewById(R.id.Total);
        }
    }
   
    
    private void populateItemRows(TodayAdapter.TodayViewHolder holder, int position) {
        TodayModel model = todayModel.get(position);
        holder.name.setText(model.getName());
        holder.id.setText(model.getProduct_id());
        holder.qty.setText(String.valueOf(Math.round(Double.parseDouble(model.getQuantity()))));
        holder.total.setText(String.valueOf(Math.round(Double.parseDouble(model.getTotal()))));

    }
    
    
    
}
