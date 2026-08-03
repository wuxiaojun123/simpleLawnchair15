package com.nice.library_splash

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

class SplashActivity : FragmentActivity(), SplashNavigator {

    private var waitingForDefaultLauncherResult = false

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

    override fun onResume() {
        super.onResume()
        if (waitingForDefaultLauncherResult && isTargetDefaultLauncher()) {
            waitingForDefaultLauncherResult = false
            finishSplash()
        }
    }

    override fun finishSplash() {
        openTargetLauncherActivity()
    }

    override fun openFallbackActivity() {
        val fallbackActivity = getActivityClassName(META_DATA_FALLBACK_ACTIVITY)
        val intent = Intent().apply {
            setClassName(packageName, fallbackActivity)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun requestSetDefaultLauncher() {
        if (isTargetDefaultLauncher()) {
            finishSplash()
            return
        }

        waitingForDefaultLauncherResult = true
        val openedSettings = startFirstAvailableActivity(
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS),
        )
        if (!openedSettings) {
            waitingForDefaultLauncherResult = false
            Toast.makeText(this, R.string.library_splash_home_settings_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startFirstAvailableActivity(vararg intents: Intent): Boolean {
        for (intent in intents) {
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                return true
            }
        }
        return false
    }
    private fun openTargetLauncherActivity() {
        val targetActivity = getActivityClassName(META_DATA_TARGET_ACTIVITY)
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName(packageName, targetActivity)
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun isTargetDefaultLauncher(): Boolean {
        val targetActivity = getActivityClassName(META_DATA_TARGET_ACTIVITY)
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val activityInfo = resolveInfo?.activityInfo ?: return false
        return activityInfo.packageName == packageName && activityInfo.name == targetActivity
    }

    private fun getActivityClassName(metaDataName: String): String {
        val activityInfo = packageManager.getActivityInfo(
            componentName,
            PackageManager.GET_META_DATA,
        )
        return activityInfo.metaData?.getString(metaDataName)
            ?: error("Missing $metaDataName meta-data")
    }

    companion object {
        const val META_DATA_TARGET_ACTIVITY = "com.nice.library_splash.TARGET_ACTIVITY"
        const val META_DATA_FALLBACK_ACTIVITY = "com.nice.library_splash.FALLBACK_ACTIVITY"
    }
}
