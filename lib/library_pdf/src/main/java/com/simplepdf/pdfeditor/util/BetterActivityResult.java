package com.simplepdf.pdfeditor.util;

import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;


public class BetterActivityResult<Input, Result> {
    private final ActivityResultLauncher<Input> launcher;
    private OnActivityResult<Result> onActivityResult;

    
    public interface OnActivityResult<O> {
        void onActivityResult(O o) throws Exception;
    }

    public static <Input, Result> BetterActivityResult<Input, Result> registerForActivityResult(ActivityResultCaller activityResultCaller, ActivityResultContract<Input, Result> activityResultContract, OnActivityResult<Result> onActivityResult) {
        return new BetterActivityResult<>(activityResultCaller, activityResultContract, onActivityResult);
    }

    public static <Input, Result> BetterActivityResult<Input, Result> registerForActivityResult(ActivityResultCaller activityResultCaller, ActivityResultContract<Input, Result> activityResultContract) {
        return registerForActivityResult(activityResultCaller, activityResultContract, null);
    }

    public static BetterActivityResult<Intent, ActivityResult> registerActivityForResult(ActivityResultCaller activityResultCaller) {
        return registerForActivityResult(activityResultCaller, new ActivityResultContracts.StartActivityForResult());
    }

    private BetterActivityResult(ActivityResultCaller activityResultCaller, ActivityResultContract<Input, Result> activityResultContract, OnActivityResult<Result> onActivityResult) {
        this.onActivityResult = onActivityResult;
        this.launcher = activityResultCaller.registerForActivityResult(activityResultContract, new ActivityResultCallback() { 
            @Override 
            public final void onActivityResult(Object obj) {
                try {
                    BetterActivityResult.this.callOnActivityResult((Result) obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setOnActivityResult(OnActivityResult<Result> onActivityResult) {
        this.onActivityResult = onActivityResult;
    }

    public void launch(Input input, OnActivityResult<Result> onActivityResult) {
        if (onActivityResult != null) {
            this.onActivityResult = onActivityResult;
        }
        this.launcher.launch(input);
    }

    public void launch(Input input) {
        launch(input, this.onActivityResult);
    }

    
    public void callOnActivityResult(Result result) throws Exception {
        OnActivityResult<Result> onActivityResult = this.onActivityResult;
        if (onActivityResult != null) {
            onActivityResult.onActivityResult(result);
        }
    }
}
