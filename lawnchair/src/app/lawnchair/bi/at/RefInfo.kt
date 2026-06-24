package app.lawnchair.bi.at

import java.io.Serializable

/**
 * Data class for install referrer information
 *
 * created on 2025/8/10
 * @author holmes
 */
data class RefInfo(
    val referrerUrl: String = "",
    val referrerClickTime: Long = 0,
    val appInstallTime: Long = 0,
    val instantExperienceLaunched: Boolean = false
) : Serializable
