package com.arodi.powergas.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arodi.powergas.R;
import com.arodi.powergas.models.PaymentModel;

import java.util.ArrayList;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<PaymentModel> paymentModel;
    Context context;
    private int selectedItemPosition = -1;
    private SparseBooleanArray selectedItems = new  SparseBooleanArray();


    public PaymentAdapter(Context context, ArrayList<PaymentModel> paymentModel) {
        this.paymentModel = paymentModel;
        this.context = context;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PaymentAdapter.PaymentViewHolder(LayoutInflater.from(context).inflate(R.layout.payment_raw, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        populateItemRows((PaymentAdapter.PaymentViewHolder) viewHolder, position);
        
    }
    
    
    
    @Override
    public int getItemCount() {
        return paymentModel.size();
    }
    
    
    
    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CheckBox checkBox;
        
        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.Payment);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    private void toggleSelection(int position) {
        selectedItemPosition = position;
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }

        notifyDataSetChanged();
    }

    public List<PaymentModel> getSelectedItems(){
        List<PaymentModel> listItems = new ArrayList<>();
        for (int i = 0; i<selectedItems.size(); i++) {
            listItems.add(paymentModel.get(selectedItems.keyAt(i)));
        }
        return  listItems;
    }
   
    
    private void populateItemRows(PaymentAdapter.PaymentViewHolder holder, int position) {
        PaymentModel model = paymentModel.get(position);
        holder.name.setText(model.getName());
        holder.checkBox.setChecked(selectedItems.get(position, false));
        holder.checkBox.setOnClickListener(v -> toggleSelection(position));
    }
    
    
    
}
