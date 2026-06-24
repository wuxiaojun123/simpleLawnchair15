package app.lawnchair.bi.a

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Utility to wait for ad platform initialization
 * 
 * created on 2025/8/10
 * @author holmes
 */
object AdPlatformWaiter {
    
    /**
     * Wait for a specific platform to be initialized
     */
    suspend fun waitForPlatform(
        mediation: AdPlatformMediation,
        timeoutMs: Long = 10_000L
    ): Boolean {
        if (mediation.isInitedSuccess()) {
            return true
        }
        
        return withTimeoutOrNull(timeoutMs) {
            mediation.initState.filter { it }.first()
            true
        } ?: false
    }
    
    /**
     * Wait for any platform in the manager to be ready
     */
    suspend fun waitForAnyPlatform(
        timeoutMs: Long = 10_000L
    ): Boolean {
        val platforms = AdPlatformManager.getRegisteredPlatforms()
        for (platform in platforms) {
            if (platform.isInitedSuccess()) {
                return true
            }
        }
        
        return withTimeoutOrNull(timeoutMs) {
            for (platform in platforms) {
                platform.initState.filter { it }.first()
                return@withTimeoutOrNull true
            }
            false
        } ?: false
    }
}
