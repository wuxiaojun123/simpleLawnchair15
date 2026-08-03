package com.simplepdf.pdfeditor.Activity;

import com.simplepdf.pdfeditor.fragment.HomeFragment;
import com.simplepdf.pdfeditor.R;

public class MainActivity extends BaseActivity {
    private static final String HOME_FRAGMENT_TAG = "HomeFragment";

    @Override
    public void setBinding() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void setToolbar() {
    }

    @Override
    public void init() {
        if (getSupportFragmentManager().findFragmentByTag(HOME_FRAGMENT_TAG) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment(), HOME_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void setViewListener() {
    }

    @Override
    public void onBackPressed() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(HOME_FRAGMENT_TAG);
        if (homeFragment != null && homeFragment.handleBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
