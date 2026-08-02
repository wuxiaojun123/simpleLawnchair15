package com.nice.library_splash

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class SplashActivity : FragmentActivity(), SplashNavigator {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.library_splash_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.splash_fragment_container,
                    SplashRegistry.createFragment(),
                )
                .commitNow()
        }
    }

    override fun finishSplash() {
        val targetActivity = getTargetActivityClassName()
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName(packageName, targetActivity)
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun getTargetActivityClassName(): String {
        val activityInfo = packageManager.getActivityInfo(
            componentName,
            PackageManager.GET_META_DATA,
        )
        return activityInfo.metaData?.getString(META_DATA_TARGET_ACTIVITY)
            ?: error("Missing $META_DATA_TARGET_ACTIVITY meta-data")
    }

    companion object {
        const val META_DATA_TARGET_ACTIVITY = "com.nice.library_splash.TARGET_ACTIVITY"
    }
}
