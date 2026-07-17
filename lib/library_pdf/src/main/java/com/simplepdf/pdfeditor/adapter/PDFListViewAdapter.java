package com.simplepdf.pdfeditor.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepdf.pdfeditor.CallbackListener.ViewCallBackListener;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ItemPdfBinding;
import com.simplepdf.pdfeditor.model.PDFFileModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



public class PDFListViewAdapter extends RecyclerView.Adapter<PDFListViewAdapter.DataViewHolder> implements Filterable {
    List<PDFFileModel> PdfFilterList;
    Context context;
    public List<PDFFileModel> list;
    public ViewCallBackListener listener;
    ArrayList<PDFFileModel> finalSelectedList = new ArrayList<>();
    String searchValue = "";
    int sortBy = 3;
    boolean selectAll = false;
    boolean selectedVisible = false;

    public PDFListViewAdapter(Context context, List<PDFFileModel> list, ViewCallBackListener viewCallBackListener) {
        this.context = context;
        this.list = list;
        this.PdfFilterList = list;
        this.listener = viewCallBackListener;
    }

    @Override 
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_pdf, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
        PDFFileModel pDFFileModel = this.PdfFilterList.get(i);
        dataViewHolder.binding.setModel(pDFFileModel);
        dataViewHolder.binding.cardParent.setCardBackgroundColor(Color.parseColor(this.finalSelectedList.contains(pDFFileModel) ? "#e9e9e9" : "#f9f9f9"));
        dataViewHolder.binding.executePendingBindings();





    }

    @Override 
    public int getItemCount() {
        return this.PdfFilterList.size();
    }

    public void setSortBy(int i) {
        this.sortBy = i;
    }

    public class AnonymousClass1 extends Filter {
        AnonymousClass1() {
        }

        @Override 
        protected FilterResults performFiltering(CharSequence charSequence) {
            String trim = charSequence.toString().trim();
            PDFListViewAdapter.this.searchValue = trim.toString().trim();
            if (trim.length() > 0) {
                ArrayList arrayList = new ArrayList();
                for (PDFFileModel pDFFileModel : PDFListViewAdapter.this.list) {
                    if (pDFFileModel.getFile().getName().toLowerCase().contains(trim.toString().toLowerCase())) {
                        arrayList.add(pDFFileModel);
                    }
                }
                PDFListViewAdapter.this.PdfFilterList = arrayList;
                Log.e("data", "Filter ");
            } else {
                PDFListViewAdapter pDFListViewAdapter = PDFListViewAdapter.this;

//                pDFListViewAdapter.PdfFilterList = (List) Collection.EL.stream(pDFListViewAdapter.list).sorted(new Comparator() {
//                    @Override
//                    public final int compare(Object obj, Object obj2) {
//                        return AnonymousClass1.this.m128xb18d330e((PDFFileModel) obj, (PDFFileModel) obj2);
//                    }
//                }).collect(Collectors.toList());

                Collections.sort(list, new Comparator<PDFFileModel>(){
                    public int compare(PDFFileModel obj1, PDFFileModel obj2) {
                        return AnonymousClass1.this.m128xb18d330e((PDFFileModel) obj1, (PDFFileModel) obj2); // To compare string values
                    }
                });

                pDFListViewAdapter.PdfFilterList = list;
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = PDFListViewAdapter.this.PdfFilterList;
            return filterResults;
        }

        
        
        public  int m128xb18d330e(PDFFileModel pDFFileModel, PDFFileModel pDFFileModel2) {
            if (PDFListViewAdapter.this.sortBy == 1) {
                return pDFFileModel.getFile().getName().compareTo(pDFFileModel2.getFile().getName());
            }
            if (PDFListViewAdapter.this.sortBy == 2) {
                return pDFFileModel2.getFile().getName().compareTo(pDFFileModel.getFile().getName());
            }
            if (PDFListViewAdapter.this.sortBy == 3) {
                int i = ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) > 0L ? 1 : ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) == 0L ? 0 : -1));
                if (i < 0) {
                    return -1;
                }
                return i > 0 ? 1 : 0;
            } else if (PDFListViewAdapter.this.sortBy == 4) {
                int i2 = ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) > 0L ? 1 : ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) == 0L ? 0 : -1));
                if (i2 < 0) {
                    return 1;
                }
                return i2 > 0 ? -1 : 0;
            } else if (PDFListViewAdapter.this.sortBy == 5) {
                return Long.compare(pDFFileModel.getFile().length(), pDFFileModel2.getFile().length());
            } else {
                if (PDFListViewAdapter.this.sortBy == 6) {
                    return Long.compare(pDFFileModel2.getFile().length(), pDFFileModel.getFile().length());
                }
                return 0;
            }
        }

        @Override 
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            PDFListViewAdapter.this.PdfFilterList = (List) filterResults.values;
            PDFListViewAdapter.this.notifyDataSetChanged();
        }
    }

    @Override 
    public Filter getFilter() {
        return new AnonymousClass1();
    }

    
    
    public class DataViewHolder extends RecyclerView.ViewHolder {
        static final  boolean $assertionsDisabled = false;
        ItemPdfBinding binding;

        public DataViewHolder(View view) {
            super(view);
            ItemPdfBinding itemPdfBinding = (ItemPdfBinding) DataBindingUtil.bind(view);
            this.binding = itemPdfBinding;
            itemPdfBinding.cardParent.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    DataViewHolder.this.m129xc59179b(view2);
                }
            });
            this.binding.mcvImgOption.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    DataViewHolder.this.m130xfdaaa71c(view2);
                }
            });
            this.binding.cardParent.setOnLongClickListener(new View.OnLongClickListener() { 
                @Override 
                public final boolean onLongClick(View view2) {
                    return DataViewHolder.this.m131xeefc369d(view2);
                }
            });
        }

        
        
        public  void m129xc59179b(View view) {
            if (PDFListViewAdapter.this.selectedVisible) {
                if (!PDFListViewAdapter.this.finalSelectedList.contains(PDFListViewAdapter.this.PdfFilterList.get(getAdapterPosition()))) {
                    PDFListViewAdapter.this.finalSelectedList.add(PDFListViewAdapter.this.PdfFilterList.get(getAdapterPosition()));
                } else {
                    PDFListViewAdapter.this.finalSelectedList.remove(PDFListViewAdapter.this.PdfFilterList.get(getAdapterPosition()));
                }
                PDFListViewAdapter.this.listener.onItemViewCount(PDFListViewAdapter.this.finalSelectedList.size());
                PDFListViewAdapter.this.notifyItemChanged(getAdapterPosition());
                return;
            }
            PDFListViewAdapter.this.listener.onItemClicked(view.getId(), getAdapterPosition());
        }

        
        
        public  void m130xfdaaa71c(View view) {
            if (PDFListViewAdapter.this.selectedVisible) {
                return;
            }
            PDFListViewAdapter.this.listener.onItemClicked(view.getId(), getAdapterPosition());
        }

        
        
        public  boolean m131xeefc369d(View view) {
            if (PDFListViewAdapter.this.selectedVisible || PDFListViewAdapter.this.listener == null) {
                return false;
            }
            PDFListViewAdapter.this.selectedVisible = true;
            PDFListViewAdapter.this.finalSelectedList.add(PDFListViewAdapter.this.PdfFilterList.get(getAdapterPosition()));
            PDFListViewAdapter.this.notifyItemChanged(getAdapterPosition());
            PDFListViewAdapter.this.listener.onItemLongClick(view.getId(), getAdapterPosition());
            return true;
        }
    }

    public void setAllSelection(boolean z) {
        this.selectAll = z;
        this.finalSelectedList.clear();
        if (this.selectAll) {
            this.finalSelectedList.addAll(this.PdfFilterList);
        }
        this.listener.onItemViewCount(this.finalSelectedList.size());
        notifyDataSetChanged();
    }

    public void setSelectAll(boolean z) {
        this.selectAll = z;
    }

    public void setList(List<PDFFileModel> list) {
        this.list = list;
        this.PdfFilterList = list;
        notifyDataSetChanged();
    }

    public List<PDFFileModel> getList() {
        return this.PdfFilterList;
    }

    public boolean isSelectedVisible() {
        return this.selectedVisible;
    }

    public void setSelectedVisible(boolean z) {
        this.selectedVisible = z;
    }

    public ArrayList<PDFFileModel> getFinalSelectedList() {
        return this.finalSelectedList;
    }
}
