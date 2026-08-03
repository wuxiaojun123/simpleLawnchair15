package com.nice.library_splash

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.didi.drouter.api.DRouter
import com.didi.drouter.api.Extend

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
        if (isTargetDefaultLauncher()) {
            openDefaultHomeFallbackActivity()
            return
        }

        val intent = SplashRegistry.createFallbackActivityIntent(this).apply {
            putExtra(EXTRA_TARGET_ACTIVITY, getTargetActivityComponent()?.className)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startWithDRouter(intent)
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

    private fun openDefaultHomeFallbackActivity() {
        val intent = SplashRegistry.createDefaultHomeFallbackActivityIntent(this).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startWithDRouter(intent)
    }

    private fun openTargetLauncherActivity() {
        val intent = SplashRegistry.createTargetActivityIntent(this).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startWithDRouter(intent)
    }

    private fun startWithDRouter(intent: Intent) {
        DRouter.build(ROUTE_START_ACTIVITY)
            .putExtra(Extend.START_ACTIVITY_VIA_INTENT, intent)
            .start(this)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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

    private fun isTargetDefaultLauncher(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }

        val targetComponent = getTargetActivityComponent() ?: return false
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val activityInfo = resolveInfo?.activityInfo ?: return false
        return activityInfo.packageName == targetComponent.packageName &&
            activityInfo.name == targetComponent.className
    }

    private fun getTargetActivityComponent(): ComponentName? {
        return SplashRegistry.createTargetActivityIntent(this).component
    }

    companion object {
        const val EXTRA_TARGET_ACTIVITY = "com.nice.library_splash.extra.TARGET_ACTIVITY"
        private const val ROUTE_START_ACTIVITY = "/library_splash/start_activity"
    }
}

