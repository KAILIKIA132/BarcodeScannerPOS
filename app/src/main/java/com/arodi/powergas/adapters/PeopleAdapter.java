package com.arodi.powergas.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.arodi.powergas.R;
import com.arodi.powergas.activities.PeopleActivity;
import com.arodi.powergas.activities.StockActivity;
import com.arodi.powergas.activities.StoreActivity;
import com.arodi.powergas.models.PeopleModel;
import com.arodi.powergas.models.StockModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class PeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<PeopleModel> peopleModel;
    Context context;


    public PeopleAdapter(Context context, ArrayList<PeopleModel> peopleModel) {
        this.peopleModel = peopleModel;
        this.context = context;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PeopleAdapter.StockViewHolder(LayoutInflater.from(context).inflate(R.layout.people_raw, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((PeopleAdapter.StockViewHolder) viewHolder, position);
        
    }
    
    
    
    @Override
    public int getItemCount() {
        return peopleModel.size();
    }
    
    
    
    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView name,plate;
        CardView cardView;
        MaterialButton next;
        
        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.Name);
            plate = itemView.findViewById(R.id.Plate);
            cardView = itemView.findViewById(R.id.card);
            next = itemView.findViewById(R.id.Next);
        }
    }
   
    
    private void populateItemRows(PeopleAdapter.StockViewHolder holder, int position) {
        PeopleModel model = peopleModel.get(position);

        if (position % 2 == 0) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.background));
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));
        }

        holder.name.setText(model.getName());
        holder.plate.setText(model.getPlate_no());

        holder.next.setOnClickListener(v -> {
            Intent intent = new Intent(context, StoreActivity.class);
            intent.putExtra("PEOPLE_INFORMATION", model);
            context.startActivity(intent);
        });

    }
    
    
    
}
