/*
 * Copyright 2024, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair.overlay

import app.lawnchair.LeftScreenFragmentResolver
import com.nice.screebkub.OverlayStateFileLogger
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewConfiguration
import android.widget.FrameLayout
import app.lawnchair.LawnchairLauncher
import com.android.launcher3.R
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.android.systemui.plugins.shared.LauncherOverlayManager.LauncherOverlay
import com.android.systemui.plugins.shared.LauncherOverlayManager.LauncherOverlayCallbacks
import com.nice.screebkub.LeftScreenFragment
import kotlin.math.abs

/**
 * Custom overlay implementation for the left screen (负一屏).
 * This shows a custom LeftScreenView when the user swipes right from the leftmost home screen.
 */
class LeftScreenOverlay(private val launcher: LawnchairLauncher) :
    LauncherOverlay,
    LauncherOverlayManager {
    companion object {
        private const val TAG = "LeftScreenOverlay"
        private const val FRAGMENT_TAG = "left_screen_fragment"
        private const val MIN_VISIBLE_PROGRESS = 0.01f
        private const val MIN_INTERACTIVE_PROGRESS = 0.18f
    }

    private var callbacks: LauncherOverlayCallbacks? = null
    private var leftScreenContainer: FrameLayout? = null
    private var isAttached = false
    private var currentProgress = 0f
    private var screenWidth = 0f
    private var hideAnimator: ValueAnimator? = null
    private val touchSlop by lazy { ViewConfiguration.get(launcher).scaledTouchSlop }
    private var gestureStartX = 0f
    private var gestureStartY = 0f
    private var gestureStartProgress = 0f

    private fun createLeftScreenView() {
        screenWidth = launcher.resources.displayMetrics.widthPixels.toFloat()

        // Create container for left screen
        leftScreenContainer = object : FrameLayout(launcher) {
            override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
                if (currentProgress <= MIN_INTERACTIVE_PROGRESS) return false
                when (ev.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        gestureStartX = ev.rawX
                        gestureStartY = ev.rawY
                        gestureStartProgress = currentProgress
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = ev.rawX - gestureStartX
                        val dy = ev.rawY - gestureStartY
                        if (abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                            startOverlayGesture()
                            return true
                        }
                    }
                }
                return super.onInterceptTouchEvent(ev)
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                if (currentProgress <= MIN_INTERACTIVE_PROGRESS) {
                    return false
                }
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        gestureStartX = event.rawX
                        gestureStartY = event.rawY
                        gestureStartProgress = currentProgress
                        startOverlayGesture()
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val progress = (gestureStartProgress
                            + (event.rawX - gestureStartX) / screenWidth).coerceIn(0f, 1f)
                        applyProgress(progress)
                        return true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        settleOverlay()
                        return true
                    }
                }
                return super.onTouchEvent(event)
            }
        }.apply {
            id = R.id.left_screen_fragment_container
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#E8F0FE"))
            // Start off-screen to the left
            translationX = -screenWidth
            visibility = View.INVISIBLE
        }

    }

    private fun attachLeftScreenView() {
        if (isAttached) {
            debugLog("attachLeftScreenView reuseAttached progress=$currentProgress")
            attachFragment()
            launcher.setLauncherOverlay(this)
            return
        }

        // Create view if not exists
        if (leftScreenContainer == null) {
            createLeftScreenView()
        }

        // Add to LauncherRootView (not DragLayer to avoid Workspace child restriction)
        val rootView = launcher.findViewById<ViewGroup>(R.id.launcher)
        if (rootView != null && leftScreenContainer != null) {
            // Add left screen container (will be on top of DragLayer, but starts off-screen)
            rootView.addView(leftScreenContainer)
            attachFragment()
            isAttached = true

            // Notify launcher that overlay is attached
            launcher.setLauncherOverlay(this)
            debugLog("attachLeftScreenView attached progress=$currentProgress childCount=${rootView.childCount}")
        }
    }

    fun ensureContentAttached() {
        attachLeftScreenView()
    }

    private fun detachLeftScreenView() {
        if (!isAttached) return

        val rootView = launcher.findViewById<ViewGroup>(R.id.launcher)
        if (rootView != null && leftScreenContainer != null) {
            rootView.removeView(leftScreenContainer)
            isAttached = false
            debugLog("detachLeftScreenView childCount=${rootView.childCount}")
        }
    }

    private fun attachFragment() {
        val fragmentManager = launcher.fragmentManager
        val existing = fragmentManager.findFragmentByTag(FRAGMENT_TAG) as? LeftScreenFragment
        val isFragmentAttachedToCurrentContainer =
            existing?.isAdded == true &&
                existing.id == R.id.left_screen_fragment_container &&
                existing.view?.parent === leftScreenContainer
        debugLog(
            "attachFragment existing=${existing != null} attachedToCurrent=$isFragmentAttachedToCurrentContainer containerExists=${leftScreenContainer != null} containerChildCount=${leftScreenContainer?.childCount} fragmentView=${existing?.view != null}",
        )
        if (isFragmentAttachedToCurrentContainer) {
            return
        }
        val transaction = fragmentManager.beginTransaction()
        if (existing != null) {
            debugLog(
                "attachFragment removingStale existingId=${existing.id} existingViewParentMatches=${existing.view?.parent === leftScreenContainer}",
            )
            transaction.remove(existing)
        }
        transaction
            .replace(
                R.id.left_screen_fragment_container,
                LeftScreenFragmentResolver.createFragment(launcher),
                FRAGMENT_TAG,
            )
            .commitAllowingStateLoss()
        fragmentManager.executePendingTransactions()
        val attached = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
        debugLog(
            "attachFragment committed attached=${attached != null} attachedToCurrent=${attached?.view?.parent === leftScreenContainer} containerChildCount=${leftScreenContainer?.childCount}",
        )
    }

    override fun onScrollInteractionBegin() {
        resetGestureTracking()
        startOverlayGesture()
    }

    override fun onScrollInteractionEnd() {
        settleOverlay()
    }

    private fun animateToProgress(targetProgress: Float) {
        hideAnimator?.cancel()

        val startProgress = currentProgress
        hideAnimator = ValueAnimator.ofFloat(startProgress, targetProgress).apply {
            duration = 200L
            addUpdateListener { animator ->
                applyProgress(animator.animatedValue as Float)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    applyProgress(targetProgress)
                    hideAnimator = null
                }
            })
            start()
        }
    }

    override fun onScrollChange(progress: Float, rtl: Boolean) {
        applyProgress(progress.coerceIn(0f, 1f))
    }

    private fun applyProgress(progress: Float) {
        currentProgress = progress
        leftScreenContainer?.let { container ->
            container.visibility = if (progress > MIN_VISIBLE_PROGRESS) View.VISIBLE else View.INVISIBLE
            container.isClickable = progress > MIN_INTERACTIVE_PROGRESS
            container.isFocusable = progress > MIN_INTERACTIVE_PROGRESS
            // Translate from -screenWidth (hidden on left) to 0 (fully visible)
            container.translationX = -screenWidth * (1f - progress)
        }
        if (progress <= MIN_VISIBLE_PROGRESS) {
            resetGestureTracking()
        }
        callbacks?.onOverlayScrollChanged(progress)
    }

    private fun startOverlayGesture() {
        hideAnimator?.cancel()
        hideAnimator = null
        leftScreenContainer?.visibility = View.VISIBLE
    }

    private fun resetGestureTracking() {
        gestureStartX = 0f
        gestureStartY = 0f
        gestureStartProgress = 0f
    }

    private fun settleOverlay() {
        val targetProgress = when {
            // When swiping back from an open left screen, allow closing with a shorter drag.
            gestureStartProgress >= 0.5f -> if (currentProgress <= 0.82f) 0f else 1f
            else -> if (currentProgress >= 0.16f) 1f else 0f
        }
        animateToProgress(targetProgress)
    }

    override fun setOverlayCallbacks(callbacks: LauncherOverlayCallbacks?) {
        this.callbacks = callbacks
    }

    // LauncherOverlayManager lifecycle methods
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        attachLeftScreenView()
    }

    override fun onActivityResumed(activity: Activity) {
        debugLog("onActivityResumed progress=$currentProgress attached=$isAttached")
        verifyLeftScreenAttachment("onActivityResumed")
    }


    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        detachLeftScreenView()
    }

    override fun onAttachedToWindow() {
        debugLog("onAttachedToWindow")
        attachLeftScreenView()
        verifyLeftScreenAttachment("onAttachedToWindow")
    }

    override fun onDetachedFromWindow() {
        debugLog("onDetachedFromWindow")
        detachLeftScreenView()
    }

    override fun openOverlay() {
        animateToProgress(1f)
    }

    override fun hideOverlay(animate: Boolean) {
        hideOverlay(if (animate) 200 else 0)
    }

    override fun hideOverlay(duration: Int) {
        if (duration > 0) {
            animateToProgress(0f)
        } else {
            applyProgress(0f)
        }
    }

    private fun verifyLeftScreenAttachment(reason: String) {
        val fragment = launcher.fragmentManager.findFragmentByTag(FRAGMENT_TAG) as? LeftScreenFragment
        val attachedToCurrentContainer =
            fragment?.isAdded == true &&
                fragment.id == R.id.left_screen_fragment_container &&
                fragment.view?.parent === leftScreenContainer
        debugLog(
            "verifyLeftScreenAttachment reason=$reason containerExists=${leftScreenContainer != null} containerChildCount=${leftScreenContainer?.childCount} fragmentExists=${fragment != null} fragmentAdded=${fragment?.isAdded} fragmentView=${fragment?.view != null} attachedToCurrent=$attachedToCurrentContainer stateSaved=${launcher.fragmentManager.isStateSaved}",
        )
        if (leftScreenContainer != null && !attachedToCurrentContainer && !launcher.fragmentManager.isStateSaved) {
            debugLog("verifyLeftScreenAttachment reattaching fragment reason=$reason")
            attachFragment()
        }
    }

    private fun debugLog(message: String) {
        OverlayStateFileLogger.log(launcher, TAG, message)
    }
}
