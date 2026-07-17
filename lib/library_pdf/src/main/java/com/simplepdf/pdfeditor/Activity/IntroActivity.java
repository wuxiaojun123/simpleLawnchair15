package com.simplepdf.pdfeditor.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ActivityIntroBinding;
import com.simplepdf.pdfeditor.databinding.DialogPermissionsBinding;
import com.simplepdf.pdfeditor.util.AppConstants;
import com.simplepdf.pdfeditor.util.AppPref;
import com.simplepdf.pdfeditor.util.BetterActivityResult;


public class IntroActivity extends BaseActivity {
    ActivityIntroBinding binding;

    @Override 
    public void setToolbar() {
    }

    @Override 
    public void setViewListener() {
    }

    @Override 
    public void setBinding() {
        this.binding = (ActivityIntroBinding) DataBindingUtil.setContentView(this, R.layout.activity_intro);
    }

    @Override 
    public void init() {
        this.binding.mcvStart.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                IntroActivity.this.m70lambda$init$0$comappworldpdfsignatureActivityIntroActivity(view);
            }
        });
    }

    
    
    public  void m70lambda$init$0$comappworldpdfsignatureActivityIntroActivity(View view) {
        GotoMainActivity();
    }

    public void GotoMainActivity() {
        AppPref.setStartApp(true);
        startActivity(new Intent(this.mContext, MainActivity.class));
        finish();
    }

    @Override 
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (iArr.length > 0 && iArr[0] == 0) {
            GotoMainActivity();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.READ_EXTERNAL_STORAGE") || ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
            alertStoragePermission(0);
        } else if (iArr[0] == -1) {
            alertStoragePermission(-1);
        } else {
            this.activityLauncher.launch(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:"+getPackageName())), new BetterActivityResult.OnActivityResult() {
                @Override 
                public final void onActivityResult(Object obj) {
                    IntroActivity.this.m71x6cf1599d((ActivityResult) obj);
                }
            });
        }
    }

    
    
    public  void m71x6cf1599d(ActivityResult activityResult) {


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            GotoMainActivity();

        }else{
            if (AppConstants.checkStoragePermissionApi19(this.mContext)) {
                GotoMainActivity();
            }
        }


    }

    public void showPermissionNotifyDialog() {
        if (Build.VERSION.SDK_INT >= 30) {
            this.activityLauncher.launch(new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION", Uri.parse("package:"+getPackageName())), new BetterActivityResult.OnActivityResult() {
                @Override 
                public final void onActivityResult(Object obj) {
                    IntroActivity.this.m72xed385f70((ActivityResult) obj);
                }
            });
        } else if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 1001);
        }
    }

    
    
    public  void m72xed385f70(ActivityResult activityResult) {
        if (AppConstants.checkStoragePermissionApi30(this.mContext)) {
            GotoMainActivity();
        }
    }

    private void alertStoragePermission(int i) {
        DialogPermissionsBinding dialogPermissionsBinding = (DialogPermissionsBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_permissions, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(dialogPermissionsBinding.getRoot());
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.show();
        dialogPermissionsBinding.txtTitle.setText("Storage permission");
        dialogPermissionsBinding.imgPermission.setVisibility(View.GONE);
        dialogPermissionsBinding.txtPermissionMsg.setText("Storage permission is required in order to provide manage and edit PDF feature, please enable permission.");
        dialogPermissionsBinding.txtCancel.setText("Ok");
        dialogPermissionsBinding.txtSave.setText("Settings");
        if (i == -1) {
            dialogPermissionsBinding.cardCancel.setVisibility(View.INVISIBLE);
        }
        dialogPermissionsBinding.cardCancel.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= 23) {
                    IntroActivity.this.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 1001);
                }
            }
        });
        dialogPermissionsBinding.cardSave.setOnClickListener(new AnonymousClass2(dialog));
    }

    
    
    
    public class AnonymousClass2 implements View.OnClickListener {
        final  Dialog val$dialog;

        AnonymousClass2(Dialog dialog) {
            this.val$dialog = dialog;
        }

        @Override 
        public void onClick(View view) {
            this.val$dialog.dismiss();
            IntroActivity.this.activityLauncher.launch(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:"+getPackageName())), new BetterActivityResult.OnActivityResult() {
                @Override 
                public final void onActivityResult(Object obj) {
                    AnonymousClass2.this.m73x21e4db2e((ActivityResult) obj);
                }
            });
        }

        
        
        public  void m73x21e4db2e(ActivityResult activityResult) {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                GotoMainActivity();

            }else{
                if (AppConstants.checkStoragePermissionApi19(IntroActivity.this.mContext)) {
                    IntroActivity.this.GotoMainActivity();
                }
            }


        }
    }

    private void openDialogPermission() {
        DialogPermissionsBinding dialogPermissionsBinding = (DialogPermissionsBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_permissions, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(dialogPermissionsBinding.getRoot());
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.show();
        Glide.with((FragmentActivity) this).load(Integer.valueOf((int) R.raw.permission_switch)).into(dialogPermissionsBinding.imgPermission);
        dialogPermissionsBinding.cardSave.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                dialog.dismiss();
                IntroActivity.this.showPermissionNotifyDialog();
            }
        });
        dialogPermissionsBinding.cardCancel.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
