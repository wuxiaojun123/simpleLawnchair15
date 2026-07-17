package com.simplepdf.pdfeditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ItemStampBinding;
import com.simplepdf.pdfeditor.model.IconBitmapModel;

import java.util.List;


public class AssertStampAdapter extends RecyclerView.Adapter<AssertStampAdapter.DataViewHolder> {
    Context context;
    List<IconBitmapModel> list;
    private OnItemClickListener mOnItemClickListener;

    
    public interface OnItemClickListener {
        void onItemClick(View view, int i);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public AssertStampAdapter(Context context, List<IconBitmapModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override 
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_stamp, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
        dataViewHolder.binding.assertImage.setImageBitmap(this.list.get(i).getIconBitmap());
    }

    @Override 
    public int getItemCount() {
        return this.list.size();
    }

    
    
    public class DataViewHolder extends RecyclerView.ViewHolder {
        ItemStampBinding binding;

        public DataViewHolder(View view) {
            super(view);
            ItemStampBinding itemStampBinding = (ItemStampBinding) DataBindingUtil.bind(view);
            this.binding = itemStampBinding;
            itemStampBinding.assertImage.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View view2) {
                    AssertStampAdapter.this.mOnItemClickListener.onItemClick(view2, DataViewHolder.this.getAdapterPosition());
                }
            });
        }
    }
}
