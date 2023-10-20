package com.example.recyclerviewcustom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Adapter2 extends RecyclerView.Adapter<Adapter2.ViewHolder> {
    LayoutInflater inflater;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null) inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_2, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tx.setText(String.format("第%d个",position));
    }

    @Override
    public int getItemCount() {
        return 30;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tx;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tx = itemView.findViewById(R.id.tx);

        }
    }

}
