package com.example.recyclerviewcustom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TestFragmentAdapter extends RecyclerView.Adapter<TestFragmentAdapter.TextFragmentViewHolder> {
    LayoutInflater inflater;
    @NonNull
    @Override
    public TextFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null) inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_fragment, parent, false);
        return new TextFragmentViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull TextFragmentViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 50;
    }

    public class TextFragmentViewHolder extends RecyclerView.ViewHolder {
        public TextFragmentViewHolder(View view) {
            super(view);
        }
    }
}
