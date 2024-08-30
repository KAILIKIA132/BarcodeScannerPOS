package com.arodi.powergas.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arodi.powergas.R;
import com.arodi.powergas.activities.ExpenseDetailActivity;
import com.arodi.powergas.models.ExpenditureModel;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpenditureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    public ArrayList<ExpenditureModel> expenditureModel;
    Context context;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    
    
    public ExpenditureAdapter(Context context, ArrayList<ExpenditureModel> expenditureModel) {
        this.expenditureModel  = expenditureModel;
        this.context = context;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ExpenditureAdapter.ExpenditureViewHolder(LayoutInflater.from(context).inflate(R.layout.expenditure_raw, parent, false));
        } else {
            return new ExpenditureAdapter.LoadingViewHolder(LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false));
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        
        if (viewHolder instanceof ExpenditureAdapter.ExpenditureViewHolder) {
            populateItemRows((ExpenditureAdapter.ExpenditureViewHolder) viewHolder, position);
        } else if (viewHolder instanceof ExpenditureAdapter.LoadingViewHolder) {
            showLoadingView((ExpenditureAdapter.LoadingViewHolder) viewHolder, position);
        }
        
    }
    
    
    
    @Override
    public int getItemCount() {
        return expenditureModel == null ? 0 : expenditureModel.size();
    }
    
    @Override
    public int getItemViewType(int position) {
        return expenditureModel.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }
    
    
    public static class ExpenditureViewHolder extends RecyclerView.ViewHolder {
        TextView day, date, name, amount, note, status, approved;
        MaterialButton more;
        
        public ExpenditureViewHolder(@NonNull View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.Day);
            date = itemView.findViewById(R.id.Date);
            name = itemView.findViewById(R.id.Name);
            amount = itemView.findViewById(R.id.Amount);
            note = itemView.findViewById(R.id.Note);
            status = itemView.findViewById(R.id.Status);
            more = itemView.findViewById(R.id.More);
            approved = itemView.findViewById(R.id.Approved);
        }
    }
    
    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        
        ProgressBar progressBar;
        
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
    
    private void showLoadingView(ExpenditureAdapter.LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
        
    }
    
    private void populateItemRows(ExpenditureAdapter.ExpenditureViewHolder holder, int position) {
        ExpenditureModel model = expenditureModel.get(position);
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(model.getDate());
            if (date!=null) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                holder.day.setText(String.valueOf(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.date.setText(model.getDate());
        holder.name.setText(model.getReference());
        holder.amount.setText("Requested: "+model.getAmount());
        holder.note.setText(model.getNote());
        holder.status.setText(model.getStatus());
        holder.approved.setText("Approved: "+model.getApproved());

        holder.more.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExpenseDetailActivity.class);
            intent.putExtra("EXPENSE_DETAIL", model);
            context.startActivity(intent);
        });
        
    }
    
    
    
}
