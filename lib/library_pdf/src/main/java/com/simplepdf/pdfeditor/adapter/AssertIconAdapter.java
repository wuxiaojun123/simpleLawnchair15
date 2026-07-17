package com.simplepdf.pdfeditor.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepdf.pdfeditor.CallbackListener.OnItemClickListener;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ItemIconBinding;

import java.util.List;


public class AssertIconAdapter extends RecyclerView.Adapter<AssertIconAdapter.DataViewHolder> {
    Context context;
    List<Bitmap> list;
    private OnItemClickListener mOnItemClickListener;

    public AssertIconAdapter(Context context, List<Bitmap> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override 
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_icon, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
        dataViewHolder.binding.assertImage.setImageBitmap(this.list.get(i));
    }

    @Override 
    public int getItemCount() {
        return this.list.size();
    }

    
    
    public class DataViewHolder extends RecyclerView.ViewHolder {
        static final  boolean $assertionsDisabled = false;
        ItemIconBinding binding;

        public DataViewHolder(View view) {
            super(view);
            ItemIconBinding itemIconBinding = (ItemIconBinding) DataBindingUtil.bind(view);
            this.binding = itemIconBinding;
            itemIconBinding.mcvAssertImage.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View view2) {
                    AssertIconAdapter.this.mOnItemClickListener.onItemClick(view2, DataViewHolder.this.getAdapterPosition());
                }
            });
        }
    }
}
