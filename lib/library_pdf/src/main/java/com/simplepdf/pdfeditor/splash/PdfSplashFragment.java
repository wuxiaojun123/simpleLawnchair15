package com.simplepdf.pdfeditor.splash;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nice.library_splash.BaseSplashFragment;
import com.simplepdf.pdfeditor.R;

public class PdfSplashFragment extends BaseSplashFragment {

    private static final long SPLASH_DELAY_MS = 1200L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable finishRunnable = this::finishSplash;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        LinearLayout contentView = new LinearLayout(requireContext());
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.CENTER);
        contentView.setPadding(dp(32), dp(32), dp(32), dp(32));
        contentView.setBackgroundColor(Color.rgb(249, 249, 249));
        contentView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ImageView iconView = new ImageView(requireContext());
        iconView.setImageResource(R.drawable.pdf);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(96), dp(96));
        iconView.setLayoutParams(iconParams);
        contentView.addView(iconView);

        TextView titleView = new TextView(requireContext());
        titleView.setText(R.string.app_name);
        titleView.setTextColor(Color.rgb(50, 55, 67));
        titleView.setTextSize(24);
        titleView.setGravity(Gravity.CENTER);
        titleView.setIncludeFontPadding(false);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(20);
        titleView.setLayoutParams(titleParams);
        contentView.addView(titleView);

        TextView subtitleView = new TextView(requireContext());
        subtitleView.setText(R.string.splash_subtitle);
        subtitleView.setTextColor(Color.rgb(124, 124, 124));
        subtitleView.setTextSize(15);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setIncludeFontPadding(false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(10);
        subtitleView.setLayoutParams(subtitleParams);
        contentView.addView(subtitleView);

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(finishRunnable, SPLASH_DELAY_MS);
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(finishRunnable);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(finishRunnable);
        super.onDestroyView();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
