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
import com.arodi.powergas.session.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;

public class StockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<StockModel> stockModel;
    Context context;
    HashMap<String, String> user = new HashMap<>();


    public StockAdapter(Context context, ArrayList<StockModel> stockModel) {
        this.stockModel = stockModel;
        this.context = context;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StockAdapter.StockViewHolder(LayoutInflater.from(context).inflate(R.layout.stock_row, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((StockAdapter.StockViewHolder) viewHolder, position);
        
    }
    
    
    
    @Override
    public int getItemCount() {
        return stockModel.size();
    }
    
    
    
    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView id,name,stock,target,price;
        
        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.Id);
            name = itemView.findViewById(R.id.Name);
            stock = itemView.findViewById(R.id.Stock);
            target = itemView.findViewById(R.id.Target);
            price = itemView.findViewById(R.id.Price);
        }
    }
   
    
    private void populateItemRows(StockAdapter.StockViewHolder holder, int position) {
        StockModel model = stockModel.get(position);
        user = new SessionManager(context).getLoginDetails();

        // Safely get the value from the HashMap and check for null
        String enableStock = user.get(SessionManager.KEY_ENABLE_STOCK);

        if ("1".equals(enableStock)) {
            holder.stock.setText(model.getStock());
        } else {
            holder.stock.setText("null");
        }
//        if (user.get(SessionManager.KEY_ENABLE_STOCK).equals("1")){
//            holder.stock.setText(model.getStock());}
//        else {
//            holder.stock.setText("null");
//        }
        holder.name.setText(model.getName());
        holder.id.setText(model.getCode());
        holder.target.setText(model.getTarget());
        holder.price.setText(model.getPrice());

    }
    
    
    
}
