package com.simplepdf.pdfeditor.Signature;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.activity.result.ActivityResult;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.simplepdf.pdfeditor.Activity.BaseActivity;
import com.simplepdf.pdfeditor.Activity.SplashActivity;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.util.BetterActivityResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class FreeHandActivity extends BaseActivity {
    private SeekBar inkWidth;
    private boolean isFreeHandCreated = false;
    private Menu menu = null;
    RadioButton radioBtnBlack;
    MaterialButton saveAButton;
    private SignatureView signatureView;

    @Override 
    public void setBinding() {
        setContentView(R.layout.activity_free_hand);
    }

    @Override 
    public void setToolbar() {
        findViewById(R.id.mcvBack).setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                FreeHandActivity.this.onBackPressed();
            }
        });
    }

    @Override 
    public void init() {
        this.signatureView = (SignatureView) findViewById(R.id.inkSignatureOverlayView);
        this.inkWidth = (SeekBar) findViewById(R.id.seekBar);
        this.saveAButton = (MaterialButton) findViewById(R.id.mBtnSave);
        this.radioBtnBlack = (RadioButton) findViewById(R.id.radioBlack);
        this.saveAButton.setEnabled(false);
    }

    @Override 
    public void setViewListener() {
        this.inkWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { 
            @Override 
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override 
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override 
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                FreeHandActivity.this.signatureView.setStrokeWidth(i);
            }
        });
        findViewById(R.id.action_clear).setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                FreeHandActivity.this.clearSignature();
                FreeHandActivity.this.enableClear(false);
                FreeHandActivity.this.enableSave(false);
            }
        });
        this.saveAButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                FreeHandActivity.this.saveFreeHand();
            }
        });
    }

    @Override 
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.freehandmenu, menu);
        this.menu = menu;
        return true;
    }

    @Override 
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        return super.onOptionsItemSelected(menuItem);
    }

    @Override 
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onRadioButtonClicked(View view) {
        boolean isChecked = ((RadioButton) view).isChecked();
        int id = view.getId();
        if (id == R.id.radioBlack) {
            if (isChecked) {
                this.signatureView.setStrokeColor(ContextCompat.getColor(this, R.color.inkblack));
            }
        } else if (id == R.id.radioRed) {
            if (isChecked) {
                this.signatureView.setStrokeColor(ContextCompat.getColor(this, R.color.inkred));
            }
        } else if (id == R.id.radioBlue) {
            if (isChecked) {
                this.signatureView.setStrokeColor(ContextCompat.getColor(this, R.color.inkblue));
            }
        } else if (id == R.id.radiogreen) {
            if (isChecked) {
                this.signatureView.setStrokeColor(ContextCompat.getColor(this, R.color.inkgreen));
            }
        } else if (id == R.id.radioCustom && isChecked) {
            this.activityLauncher.launch(new Intent(getApplicationContext(), colorPickerActivity.class), new BetterActivityResult.OnActivityResult() {
                @Override 
                public final void onActivityResult(Object obj) {
                    FreeHandActivity.this.m121x78ed6618((ActivityResult) obj);
                }
            });
        }
    }

    
    
    public  void m121x78ed6618(ActivityResult activityResult) {
        if (activityResult.getResultCode() == -1) {
            Intent data = activityResult.getData();
            if (data != null) {
                this.signatureView.setStrokeColor(data.getIntExtra("pickColor", getResources().getColor(R.color.inkblack)));
                return;
            }
            return;
        }
        this.radioBtnBlack.setChecked(true);
        this.signatureView.setStrokeColor(ContextCompat.getColor(this, R.color.inkblack));
    }

    @Override 
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public void clearSignature() {
        this.signatureView.clear();
        this.signatureView.setEditable(true);
    }

    public void enableClear(boolean z) {
        ImageView imageView = (ImageView) findViewById(R.id.imgAction_clear);
        ((MaterialCardView) findViewById(R.id.action_clear)).setEnabled(z);
        if (z) {
            imageView.setAlpha(1.0f);
        } else {
            imageView.setAlpha(0.5f);
        }
    }

    public void enableSave(boolean z) {
        this.saveAButton.setEnabled(z);
    }

    public void saveFreeHand() {
        SignatureView signatureView = (SignatureView) findViewById(R.id.inkSignatureOverlayView);
        ArrayList<ArrayList<Float>> arrayList = signatureView.mInkList;
        if (arrayList != null && arrayList.size() > 0) {
            this.isFreeHandCreated = true;
        }
        String saveAsImageBitmap = SignatureUtils.saveAsImageBitmap(getApplicationContext(), signatureView);
        Intent intent = new Intent();
        intent.putExtra("newSing", saveAsImageBitmap);
        SplashActivity.isRate = true;
        setResult(-1, intent);
        finish();
        signatureView.getImage();
    }

    public Uri getImageUri(Context context, Bitmap bitmap) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new ByteArrayOutputStream());
        return Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", (String) null));
    }
}
