package app.lawnchair.bi.at

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Install Referrer client for attribution
 * Uses Google Play Install Referrer API to get install attribution data
 *
 * created on 2025/8/10
 * @author holmes
 */
object InsRef {

    private const val TAG = "insref"
    private const val PREF_NAME = "lawnchair_referrer"
    private const val KEY_REFERRER_URL = "referrer_url"
    private const val KEY_REFERRER_CLICK_TIME = "referrer_click_time"
    private const val KEY_APP_INSTALL_TIME = "app_install_time"
    private const val KEY_INSTANT_EXPERIENCE = "instant_experience"
    private const val KEY_IS_ORGANIC = "is_organic"
    private const val KEY_FETCHED = "fetched"

    private val initBarrier = AtomicBoolean(false)
    private var referrerClient: InstallReferrerClient? = null

    @Volatile
    private var refInfo: RefInfo? = null

    @Volatile
    private var isOrganic: Boolean = true

    interface RefCallback {
        fun onRefReceived(info: RefInfo, isOrganic: Boolean)
        fun onRefFailed(msg: String)
    }

    fun initialize(context: Context, callback: RefCallback? = null) {
        if (!initBarrier.compareAndSet(false, true)) {
            // Already initialized, return cached if available
            refInfo?.let {
                callback?.onRefReceived(it, isOrganic)
            }
            return
        }

        val prefs = getPrefs(context)

        // Check if already fetched
        if (prefs.getBoolean(KEY_FETCHED, false)) {
            refInfo = loadFromPrefs(prefs)
            isOrganic = prefs.getBoolean(KEY_IS_ORGANIC, true)
            Log.d(TAG, "Loaded cached referrer: ${refInfo?.referrerUrl}, organic=$isOrganic")
            callback?.onRefReceived(refInfo!!, isOrganic)
            return
        }

        try {
            referrerClient = InstallReferrerClient.newBuilder(context).build()
            referrerClient?.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            try {
                                val response = referrerClient?.installReferrer
                                if (response != null) {
                                    val info = RefInfo(
                                        referrerUrl = response.installReferrer ?: "",
                                        referrerClickTime = response.referrerClickTimestampSeconds,
                                        appInstallTime = response.installBeginTimestampSeconds,
                                        instantExperienceLaunched = response.googlePlayInstantParam
                                    )
                                    refInfo = info
                                    isOrganic = checkOrganic(info.referrerUrl)
                                    saveToPrefs(prefs, info, isOrganic)

                                    Log.d(TAG, "Referrer received: ${info.referrerUrl}, organic=$isOrganic")
                                    callback?.onRefReceived(info, isOrganic)
                                } else {
                                    Log.w(TAG, "Install referrer response is null")
                                    callback?.onRefFailed("Response null")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error getting referrer", e)
                                callback?.onRefFailed(e.message ?: "Unknown error")
                            } finally {
                                referrerClient?.endConnection()
                            }
                        }

                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            Log.w(TAG, "Install referrer not supported")
                            callback?.onRefFailed("Not supported")
                        }

                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            Log.w(TAG, "Install referrer service unavailable")
                            callback?.onRefFailed("Service unavailable")
                        }

                        else -> {
                            Log.w(TAG, "Install referrer unknown response: $responseCode")
                            callback?.onRefFailed("Unknown response: $responseCode")
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    Log.d(TAG, "Install referrer service disconnected")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error starting referrer client", e)
            callback?.onRefFailed(e.message ?: "Unknown error")
        }
    }

    fun getRefInfo(): RefInfo? = refInfo

    fun isOrganic(): Boolean = isOrganic

    private fun checkOrganic(referrerUrl: String): Boolean {
        if (referrerUrl.isEmpty()) return true
        return try {
            val uri = Uri.parse("https://play.google.com/store/apps/details?$referrerUrl")
            val utmMedium = uri.getQueryParameter("utm_medium")
            utmMedium.isNullOrEmpty() || utmMedium == "organic"
        } catch (e: Exception) {
            true
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun saveToPrefs(prefs: SharedPreferences, info: RefInfo, organic: Boolean) {
        prefs.edit()
            .putString(KEY_REFERRER_URL, info.referrerUrl)
            .putLong(KEY_REFERRER_CLICK_TIME, info.referrerClickTime)
            .putLong(KEY_APP_INSTALL_TIME, info.appInstallTime)
            .putBoolean(KEY_INSTANT_EXPERIENCE, info.instantExperienceLaunched)
            .putBoolean(KEY_IS_ORGANIC, organic)
            .putBoolean(KEY_FETCHED, true)
            .apply()
    }

    private fun loadFromPrefs(prefs: SharedPreferences): RefInfo {
        return RefInfo(
            referrerUrl = prefs.getString(KEY_REFERRER_URL, "") ?: "",
            referrerClickTime = prefs.getLong(KEY_REFERRER_CLICK_TIME, 0),
            appInstallTime = prefs.getLong(KEY_APP_INSTALL_TIME, 0),
            instantExperienceLaunched = prefs.getBoolean(KEY_INSTANT_EXPERIENCE, false)
        )
    }
}
