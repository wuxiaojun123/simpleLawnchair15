package com.simplepdf.pdfeditor.Activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simplepdf.pdfeditor.CallbackListener.RecycleViewCallBackListener;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.adapter.ChoosePDFListViewAdapter;
import com.simplepdf.pdfeditor.databinding.ActivityChoosePdfBinding;
import com.simplepdf.pdfeditor.model.FileListModel;
import com.simplepdf.pdfeditor.util.DocumentFetcher;

import java.util.ArrayList;
import java.util.List;


public class ChoosePdfActivity extends BaseActivity {
    ChoosePDFListViewAdapter adapter;
    ActivityChoosePdfBinding binding;
    SearchView searchView;
    List<FileListModel> filesList = new ArrayList();
    public boolean isFilter = false;

    @Override 
    public void setViewListener() {
    }

    @Override 
    public void setBinding() {
        ActivityChoosePdfBinding activityChoosePdfBinding = (ActivityChoosePdfBinding) DataBindingUtil.setContentView(this, R.layout.activity_choose_pdf);
        this.binding = activityChoosePdfBinding;
    }

    @Override 
    public void setToolbar() {
        try {
            setSupportActionBar(this.binding.toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            this.binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override 
    public void init() {
//        AdAdmob adAdmob = new AdAdmob( this);
//        adAdmob.BannerAd(binding.banner, this);

        this.binding.rvChoosePdfList.setHasFixedSize(true);
        this.binding.rvChoosePdfList.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new ChoosePDFListViewAdapter(this, this.filesList, new RecycleViewCallBackListener() { 
            @Override 
            public final void onItemClicked(int i, int i2) {
                ChoosePdfActivity.this.m52x12f4842e(i, i2);
            }
        });
        this.binding.rvChoosePdfList.setAdapter(this.adapter);
        this.binding.pdfProgress.setVisibility(View.VISIBLE);
        new DocumentFetcher(this, DocumentFetcher.PDF, DocumentFetcher.PDF, "", DocumentFetcher.ORDER_AZ, new DocumentFetcher.OnFileFetchListnear() {
            @Override 
            public final void onFileFetched(List list) {
                ChoosePdfActivity.this.m53x2d1002cd(list);
            }
        });
    }

    
    
    public  void m52x12f4842e(int i, int i2) {
        Intent intent = new Intent();
        intent.putExtra("choosePdf", this.adapter.getList().get(i2).getFilePath());
        setResult(-1, intent);
        finish();
    }

    public  void m53x2d1002cd(List list) {
        this.filesList.addAll(list);
        ChoosePDFListViewAdapter choosePDFListViewAdapter = this.adapter;
        if (choosePDFListViewAdapter != null) {
            choosePDFListViewAdapter.sortDateDesc(this.filesList);
            this.adapter.setList(this.filesList);
            this.binding.pdfProgress.setVisibility(View.GONE);
        }
    }

    @Override 
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pro_menu, menu);
        menu.findItem(R.id.menu_pro).setVisible(false);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        this.searchView = searchView;
        EditText editText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setTextColor(getResources().getColor(R.color.textColor1));
        editText.setHintTextColor(getResources().getColor(R.color.textColor2));
        ((ImageView) this.searchView.findViewById(androidx.appcompat.R.id.search_button)).setImageResource(R.drawable.menu_search);
        this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { 
            @Override 
            public boolean onQueryTextSubmit(String str) {
                return false;
            }

            @Override 
            public boolean onQueryTextChange(String str) {
                ChoosePdfActivity.this.isFilter = !TextUtils.isEmpty(str.trim());
                if (ChoosePdfActivity.this.adapter != null) {
                    ChoosePdfActivity.this.adapter.getFilter().filter(str);
                    return false;
                }
                return false;
            }
        });
        return true;
    }

    @Override 
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        return super.onOptionsItemSelected(menuItem);
    }
}
