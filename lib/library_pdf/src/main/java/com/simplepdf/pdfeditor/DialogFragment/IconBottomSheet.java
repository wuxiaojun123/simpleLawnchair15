package com.simplepdf.pdfeditor.DialogFragment;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.simplepdf.pdfeditor.CallbackListener.BottomSheetItemClick;
import com.simplepdf.pdfeditor.CallbackListener.OnItemClickListener;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.adapter.AssertIconAdapter;
import com.simplepdf.pdfeditor.databinding.BottomsheetIconBinding;

import java.io.IOException;
import java.util.ArrayList;


public class IconBottomSheet extends PdfBottomSheetDialogFragment {
    public static BottomSheetItemClick listener;
    AssertIconAdapter adapter;
    AssetManager assetManager;
    BottomsheetIconBinding binding;

    public static IconBottomSheet newInstance(BottomSheetItemClick bottomSheetItemClick) {
        IconBottomSheet iconBottomSheet = new IconBottomSheet();
        listener = bottomSheetItemClick;
        return iconBottomSheet;
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        BottomsheetIconBinding bottomsheetIconBinding = (BottomsheetIconBinding) DataBindingUtil.inflate(pdfThemeInflater(layoutInflater), R.layout.bottomsheet_icon, viewGroup, false);
        this.binding = bottomsheetIconBinding;
        View root = bottomsheetIconBinding.getRoot();
        this.assetManager = getActivity().getAssets();
        initView();
        return root;
    }

    public void initView() {
        String[] list;
        final ArrayList arrayList = new ArrayList();
        try {
            for (String str : this.assetManager.list("img")) {
                arrayList.add(BitmapFactory.decodeStream(this.assetManager.open("img/" + str)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.binding.rvIconRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        this.adapter = new AssertIconAdapter(getActivity(), arrayList, new OnItemClickListener() {
            @Override 
            public void onItemClick(View view, int i) {
                IconBottomSheet.listener.onItemClick((Bitmap) arrayList.get(i));
                IconBottomSheet.this.dismiss();
            }
        });
        this.binding.rvIconRecyclerView.setAdapter(this.adapter);
    }
}
