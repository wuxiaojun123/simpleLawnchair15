package com.simplepdf.pdfeditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepdf.pdfeditor.CallbackListener.RecycleViewCallBackListener;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ItemDateformatBinding;
import com.simplepdf.pdfeditor.util.AppConstants;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class DateFormatAdapter extends RecyclerView.Adapter<DateFormatAdapter.DataViewHolder> {
    Context context;
    long currentMillis;
    List<String> list;
    RecycleViewCallBackListener listener;

    public DateFormatAdapter(Context context, List<String> list, RecycleViewCallBackListener recycleViewCallBackListener) {
        this.currentMillis = 0L;
        this.context = context;
        this.list = list;
        this.listener = recycleViewCallBackListener;
        this.currentMillis = System.currentTimeMillis();
    }

    @Override 
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dateformat, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
        dataViewHolder.binding.ItemTextview.setText(AppConstants.getFormattedDate(this.currentMillis, new SimpleDateFormat(this.list.get(i), Locale.getDefault())));
    }

    @Override 
    public int getItemCount() {
        return this.list.size();
    }

    public void setCurrentMillis(long j) {
        this.currentMillis = j;
        notifyDataSetChanged();
    }

    public long getCurrentMillis() {
        return this.currentMillis;
    }

    
    
    public class DataViewHolder extends RecyclerView.ViewHolder {
        ItemDateformatBinding binding;

        public DataViewHolder(View view) {
            super(view);
            ItemDateformatBinding itemDateformatBinding = (ItemDateformatBinding) DataBindingUtil.bind(view);
            this.binding = itemDateformatBinding;
            itemDateformatBinding.cdText.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View view2) {
                    DateFormatAdapter.this.listener.onItemClicked(view2.getId(), DataViewHolder.this.getAdapterPosition());
                }
            });
        }
    }
}
