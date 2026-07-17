package com.simplepdf.pdfeditor.Signature;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepdf.pdfeditor.Activity.BaseActivity;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.adapter.SignatureRecycleViewAdapter;
import com.simplepdf.pdfeditor.databinding.ActivitySignatureBinding;
import com.simplepdf.pdfeditor.databinding.SignatureDeletetDialogBinding;
import com.simplepdf.pdfeditor.util.BetterActivityResult;
import com.simplepdf.pdfeditor.util.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SignatureActivity extends BaseActivity {
    ActivitySignatureBinding binding;
    boolean fromMenu;
    private boolean hasChanges;
    List<File> items = null;
    private SignatureRecycleViewAdapter mAdapter;
    String message;

    @Override
    public void setBinding() {
        ActivitySignatureBinding activitySignatureBinding = (ActivitySignatureBinding) DataBindingUtil.setContentView(this, R.layout.activity_signature);
        this.binding = activitySignatureBinding;
        Intent intent = getIntent();
        this.message = intent.getStringExtra("ActivityAction");
        this.fromMenu = intent.getBooleanExtra("FromMenu", false);
    }

    @Override
    public void setToolbar() {
        setActionBarToolbar(true, "Signature", this.binding.toolbar);
    }

    @Override
    public void init() {

//        AdAdmob adAdmob = new AdAdmob( this);
//        adAdmob.BannerAd(binding.banner, this);
        setRecycleViewer();
    }

    @Override
    public void setViewListener() {
        this.binding.fabSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                SignatureActivity.this.m125xf1f23303(view);
            }
        });
    }


    public void m125xf1f23303(View view) {

        this.activityLauncher.launch(new Intent(getApplicationContext(), FreeHandActivity.class), new BetterActivityResult.OnActivityResult() {
            @Override
            public final void onActivityResult(Object obj) {
                SignatureActivity.this.m124xc89dddc2((ActivityResult) obj);
            }
        });
    }


    public void m124xc89dddc2(ActivityResult activityResult) {
        if (activityResult.getResultCode() == Activity.RESULT_OK) {
            markChanged();
        }
        CreateDataSource();
    }


    private void markChanged() {
        this.hasChanges = true;
        if (this.fromMenu) {
            Intent intent = new Intent();
            intent.putExtra("SignatureChanged", true);
            setResult(Activity.RESULT_OK, intent);
        }
    }

    private void setRecycleViewer() {
        this.binding.mainRecycleView.setLayoutManager(new LinearLayoutManager(this));
        SignatureRecycleViewAdapter signatureRecycleViewAdapter = new SignatureRecycleViewAdapter(this, this.items);
        this.mAdapter = signatureRecycleViewAdapter;
        signatureRecycleViewAdapter.setOnItemClickListener(new SignatureRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i, int i2, View view) {
                if (i != R.id.freeHandItem) {
                    if (i == R.id.mcvSignOption) {
                        SignatureActivity signatureActivity = SignatureActivity.this;
                        signatureActivity.OpenOptionMenu(i2, signatureActivity.items.get(i2), view);
                    }
                } else if (SignatureActivity.this.fromMenu) {
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("FileName", SignatureActivity.this.items.get(i2).getPath());
                    SignatureActivity.this.setResult(-1, intent);
                    SignatureActivity.this.finish();
                }
            }
        });
        this.binding.mainRecycleView.setAdapter(this.mAdapter);
        this.binding.mainRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                if (i2 > 0) {
                    SignatureActivity.this.binding.fabSignature.hide();
                } else {
                    SignatureActivity.this.binding.fabSignature.show();
                }
                super.onScrolled(recyclerView, i, i2);
            }
        });
        CreateDataSource();
    }

    public void OpenOptionMenu(final int i, final File file, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_sign_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public final boolean onMenuItemClick(MenuItem menuItem) {
                return SignatureActivity.this.m122xa2a83821(i, file, menuItem);
            }
        });
        popupMenu.show();
    }


    public boolean m122xa2a83821(int i, File file, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_delete) {
            deleteSignature(i, file);
            return true;
        }
        return false;
    }

    private void CreateDataSource() {
        this.items = new ArrayList();
        File file = new File(Constant.SignaturePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            // List<File> list = (List) Collection.EL.stream(Arrays.asList(listFiles)).sorted(SignatureActivity$$ExternalSyntheticLambda5.INSTANCE).collect(Collectors.toList());

            List<File> list = Arrays.asList(listFiles);

            this.items = list;
            SignatureRecycleViewAdapter signatureRecycleViewAdapter = this.mAdapter;
            if (signatureRecycleViewAdapter != null) {
                signatureRecycleViewAdapter.setList(list);
                this.mAdapter.notifyDataSetChanged();
                noDataView();
            }
        }
    }


    public static int lambda$CreateDataSource$4(File file, File file2) {
        int i = ((file2.lastModified() - file.lastModified()) > 0L ? 1 : ((file2.lastModified() - file.lastModified()) == 0L ? 0 : -1));
        if (i < 0) {
            return -1;
        }
        return i > 0 ? 1 : 0;
    }

    public void noDataView() {
        this.binding.toDoEmptyView.setVisibility(this.items.size() > 0 ? View.GONE : View.VISIBLE);
    }

    public void deleteSignature(final int i, final File file) {
        SignatureDeletetDialogBinding signatureDeletetDialogBinding = (SignatureDeletetDialogBinding) DataBindingUtil.inflate(LayoutInflater.from(this.mContext), R.layout.signature_deletet_dialog, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        signatureDeletetDialogBinding.txtDescription.setText("Do you really want to delete\nthese signature?");
        dialog.setContentView(signatureDeletetDialogBinding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.setCancelable(false);
        signatureDeletetDialogBinding.cardSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                SignatureActivity.this.m123xfd4afc18(file, i, dialog, view);
            }
        });
        signatureDeletetDialogBinding.cardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public void m123xfd4afc18(File file, int i, Dialog dialog, View view) {
        try {
            if (file.exists()) {
                file.delete();
                markChanged();
            }
            CreateDataSource();
            dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
