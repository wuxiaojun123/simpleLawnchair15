package com.simplepdf.pdfeditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.SignatureItemBinding;

import java.io.File;
import java.util.List;


public class SignatureRecycleViewAdapter extends RecyclerView.Adapter<SignatureRecycleViewAdapter.DataViewHolder> {
    private Context context;
    private OnItemClickListener onClickListener = null;
    private List<File> signatures;

    
    public interface OnItemClickListener {
        void onItemClick(int i, int i2, View view);
    }

    public SignatureRecycleViewAdapter(Context context, List<File> list) {
        this.signatures = list;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onClickListener = onItemClickListener;
    }

    @Override 
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.signature_item, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
        Glide.with(this.context).load(this.signatures.get(i).getPath()).into(dataViewHolder.binding.imgView);
    }

    @Override 
    public int getItemCount() {
        return this.signatures.size();
    }

    public void setList(List<File> list) {
        this.signatures = list;
    }

    public List<File> getList() {
        return this.signatures;
    }

    
    
    public class DataViewHolder extends RecyclerView.ViewHolder {
        static final  boolean $assertionsDisabled = false;
        SignatureItemBinding binding;

        public DataViewHolder(View view) {
            super(view);
            SignatureItemBinding signatureItemBinding = (SignatureItemBinding) DataBindingUtil.bind(view);
            this.binding = signatureItemBinding;
            signatureItemBinding.freeHandItem.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View view2) {
                    SignatureRecycleViewAdapter.this.onClickListener.onItemClick(view2.getId(), DataViewHolder.this.getAdapterPosition(), view2);
                }
            });
            this.binding.mcvSignOption.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View view2) {
                    SignatureRecycleViewAdapter.this.onClickListener.onItemClick(view2.getId(), DataViewHolder.this.getAdapterPosition(), view2);
                }
            });
        }
    }
}
