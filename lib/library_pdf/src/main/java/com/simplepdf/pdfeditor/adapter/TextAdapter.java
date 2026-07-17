package com.simplepdf.pdfeditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepdf.pdfeditor.CallbackListener.RecycleListener;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ItemTextBinding;

import java.util.List;


public class TextAdapter extends RecyclerView.Adapter<TextAdapter.DataViewHolder> {
    Context context;
    List<String> list;
    RecycleListener listener;

    public TextAdapter(Context context, List<String> list, RecycleListener recycleListener) {
        this.context = context;
        this.list = list;
        this.listener = recycleListener;
    }

    @Override 
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_text, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
        dataViewHolder.binding.ItemTextview.setText(this.list.get(i));
    }

    @Override 
    public int getItemCount() {
        return this.list.size();
    }

    
    
    public class DataViewHolder extends RecyclerView.ViewHolder {
        ItemTextBinding binding;

        public DataViewHolder(View view) {
            super(view);
            ItemTextBinding itemTextBinding = (ItemTextBinding) DataBindingUtil.bind(view);
            this.binding = itemTextBinding;
            itemTextBinding.cdText.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View view2) {
                    TextAdapter.this.listener.onItemClicked(view2.getId(), DataViewHolder.this.getAdapterPosition(), view2);
                }
            });
            this.binding.mcvTextOption.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View view2) {
                    TextAdapter.this.listener.onItemClicked(view2.getId(), DataViewHolder.this.getAdapterPosition(), view2);
                }
            });
        }
    }
}
