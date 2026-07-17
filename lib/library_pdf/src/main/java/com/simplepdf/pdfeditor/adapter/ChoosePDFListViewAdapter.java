package com.simplepdf.pdfeditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepdf.pdfeditor.CallbackListener.RecycleViewCallBackListener;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ItemAllPdfBinding;
import com.simplepdf.pdfeditor.model.FileListModel;
import com.simplepdf.pdfeditor.util.AppConstants;
import com.simplepdf.pdfeditor.util.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ChoosePDFListViewAdapter extends RecyclerView.Adapter<ChoosePDFListViewAdapter.DataViewHolder> implements Filterable {
    List<FileListModel> PdfFilterList;
    Context context;
    public List<FileListModel> list;
    public RecycleViewCallBackListener listener;
    String searchValue = "";
    boolean selectedVisible = false;

    public ChoosePDFListViewAdapter(Context context, List<FileListModel> list, RecycleViewCallBackListener recycleViewCallBackListener) {
        this.context = context;
        this.list = list;
        this.PdfFilterList = list;
        this.listener = recycleViewCallBackListener;
    }

    @Override 
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_all_pdf, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
        FileListModel fileListModel = this.PdfFilterList.get(i);
        dataViewHolder.binding.fileItemTextview.setText(fileListModel.getFilename());

        File file1 = new File(fileListModel.getFilePath());
        long time1 = file1.lastModified();


        dataViewHolder.binding.dateItemTimeTextView.setText(String.format("%s %s", AppConstants.FileSizeWithUnits(fileListModel.getFileSize()), AppConstants.getFormattedDate(time1, Constant.FILE_DATE_FORMAT)));
        dataViewHolder.binding.executePendingBindings();
    }

    @Override 
    public int getItemCount() {
        return this.PdfFilterList.size();
    }

    
    
    class AnonymousClass1 extends Filter {
        AnonymousClass1() {
        }

        @Override 
        protected FilterResults performFiltering(CharSequence charSequence) {
            String trim = charSequence.toString().trim();
            ChoosePDFListViewAdapter.this.searchValue = trim.toString().trim();
            if (trim.length() > 0) {
                ArrayList arrayList = new ArrayList();
                for (FileListModel fileListModel : ChoosePDFListViewAdapter.this.list) {
                    if (fileListModel.getFilename().toLowerCase().contains(trim.toString().toLowerCase())) {
                        arrayList.add(fileListModel);
                    }
                }
                ChoosePDFListViewAdapter.this.PdfFilterList = arrayList;
            } else {
                ChoosePDFListViewAdapter choosePDFListViewAdapter = ChoosePDFListViewAdapter.this;
                choosePDFListViewAdapter.PdfFilterList = choosePDFListViewAdapter.list;
                Collections.sort(ChoosePDFListViewAdapter.this.PdfFilterList, ChoosePDFListViewAdapter1.INSTANCE);
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = ChoosePDFListViewAdapter.this.PdfFilterList;
            return filterResults;
        }

        @Override 
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            ChoosePDFListViewAdapter.this.PdfFilterList = (List) filterResults.values;
            ChoosePDFListViewAdapter.this.notifyDataSetChanged();
        }
    }

    @Override 
    public Filter getFilter() {
        return new AnonymousClass1();
    }

    
    
    public class DataViewHolder extends RecyclerView.ViewHolder {
        static final  boolean $assertionsDisabled = false;
        ItemAllPdfBinding binding;

        public DataViewHolder(View view) {
            super(view);
            ItemAllPdfBinding itemAllPdfBinding = (ItemAllPdfBinding) DataBindingUtil.bind(view);
            this.binding = itemAllPdfBinding;
            itemAllPdfBinding.cardParent.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    DataViewHolder.this.m127xe2475fc4(view2);
                }
            });
        }

        
        
        public  void m127xe2475fc4(View view) {
            ChoosePDFListViewAdapter.this.listener.onItemClicked(view.getId(), getAdapterPosition());
        }
    }

    public void sortDateDesc(List<FileListModel> list) {
       // Collections.sort(list, ChoosePDFListViewAdapter$$ExternalSyntheticLambda0.INSTANCE);
    }

    public void setList(List<FileListModel> list) {
        this.PdfFilterList = list;
        notifyDataSetChanged();
    }

    public List<FileListModel> getList() {
        return this.PdfFilterList;
    }
}
