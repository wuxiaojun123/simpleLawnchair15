package com.simplepdf.pdfeditor.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;


public class RecyclerViewEmptySupport extends RecyclerView {
    private View emptyView;
    private AdapterDataObserver observer;

    public RecyclerViewEmptySupport(Context context) {
        super(context);
        this.observer = new AdapterDataObserver() { 
            @Override 
            public void onChanged() {
                RecyclerViewEmptySupport.this.showEmptyView();
            }

            @Override 
            public void onItemRangeInserted(int i, int i2) {
                super.onItemRangeInserted(i, i2);
                RecyclerViewEmptySupport.this.showEmptyView();
            }

            @Override 
            public void onItemRangeRemoved(int i, int i2) {
                super.onItemRangeRemoved(i, i2);
                RecyclerViewEmptySupport.this.showEmptyView();
            }
        };
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.observer = new AdapterDataObserver() { 
            @Override 
            public void onChanged() {
                RecyclerViewEmptySupport.this.showEmptyView();
            }

            @Override 
            public void onItemRangeInserted(int i, int i2) {
                super.onItemRangeInserted(i, i2);
                RecyclerViewEmptySupport.this.showEmptyView();
            }

            @Override 
            public void onItemRangeRemoved(int i, int i2) {
                super.onItemRangeRemoved(i, i2);
                RecyclerViewEmptySupport.this.showEmptyView();
            }
        };
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.observer = new AdapterDataObserver() { 
            @Override 
            public void onChanged() {
                RecyclerViewEmptySupport.this.showEmptyView();
            }

            @Override 
            public void onItemRangeInserted(int i2, int i22) {
                super.onItemRangeInserted(i2, i22);
                RecyclerViewEmptySupport.this.showEmptyView();
            }

            @Override 
            public void onItemRangeRemoved(int i2, int i22) {
                super.onItemRangeRemoved(i2, i22);
                RecyclerViewEmptySupport.this.showEmptyView();
            }
        };
    }

    public void showEmptyView() {
        Adapter adapter = getAdapter();
        if (adapter == null || this.emptyView == null) {
            return;
        }
        if (adapter.getItemCount() == 0) {
            this.emptyView.setVisibility(View.VISIBLE);
            setVisibility(View.GONE);
            return;
        }
        this.emptyView.setVisibility(View.GONE);
        setVisibility(View.VISIBLE);
    }

    @Override 
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(this.observer);
            this.observer.onChanged();
        }
    }

    public void setEmptyView(View view) {
        this.emptyView = view;
    }
}
