/*
 * Copyright 2022, Lawnchair
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

package app.lawnchair

import android.animation.AnimatorSet
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Pair
import android.view.Display
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import android.view.ViewTreeObserver
import android.window.SplashScreen
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import app.lawnchair.LawnchairApp.Companion.showQuickstepWarningIfNecessary
import app.lawnchair.compat.LawnchairQuickstepCompat
import app.lawnchair.data.AppDatabase
import app.lawnchair.data.wallpaper.service.WallpaperService
import app.lawnchair.factory.LawnchairWidgetHolder
import app.lawnchair.gestures.GestureController
import app.lawnchair.gestures.VerticalSwipeTouchController
import app.lawnchair.gestures.config.GestureHandlerConfig
import app.lawnchair.nexuslauncher.OverlayCallbackImpl
import app.lawnchair.overlay.LeftScreenOverlay
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.root.RootHelperManager
import app.lawnchair.root.RootNotAvailableException
import com.nice.screebkub.LeftScreenHostActions
import com.nice.screebkub.OverlayStateFileLogger
import com.nice.screebkub.RightScreenFragment
import app.lawnchair.theme.ThemeProvider
import app.lawnchair.ui.popup.LauncherOptionsPopup
import app.lawnchair.ui.popup.LawnchairShortcut
import app.lawnchair.util.getThemedIconPacksInstalled
import app.lawnchair.util.unsafeLazy
import app.lawnchair.views.LawnchairFloatingSurfaceView
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.BaseActivity
import com.android.launcher3.BubbleTextView
import com.android.launcher3.CellLayout
import com.android.launcher3.GestureNavContract
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.celllayout.CellLayoutLayoutParams
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.shortcuts.DeepShortcutView
import com.android.launcher3.statemanager.StateManager
import com.android.launcher3.statemanager.StateManager.StateHandler
import com.android.launcher3.uioverrides.QuickstepLauncher
import com.android.launcher3.uioverrides.states.AllAppsState
import com.android.launcher3.uioverrides.states.BackgroundAppState
import com.android.launcher3.uioverrides.states.OverviewState
import com.android.launcher3.util.ActivityOptionsWrapper
import com.android.launcher3.util.Executors
import com.android.launcher3.util.RunnableList
import com.android.launcher3.util.SystemUiController.UI_STATE_BASE_WINDOW
import com.android.launcher3.util.Themes
import com.android.launcher3.util.TouchController
import com.android.launcher3.views.ActivityContext
import com.android.launcher3.views.OptionsPopupView
import com.android.launcher3.views.OptionsPopupView.OptionItem
import com.android.launcher3.widget.LauncherWidgetHolder
import com.android.launcher3.widget.RoundedCornerEnforcement
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.android.systemui.shared.system.QuickStepContract
import com.kieronquinn.app.smartspacer.sdk.client.SmartspacerClient
import com.patrykmichalik.opto.core.firstBlocking
import com.patrykmichalik.opto.core.onEach
import dev.kdrag0n.monet.theme.ColorScheme
import java.util.stream.Stream
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LawnchairLauncher : QuickstepLauncher(), LeftScreenHostActions {
    private val defaultOverlay by unsafeLazy { LeftScreenOverlay(this) }
    private val googleFeedOverlay by unsafeLazy { OverlayCallbackImpl(this) }
    private val prefs by unsafeLazy { PreferenceManager.getInstance(this) }
    private val preferenceManager2 by unsafeLazy { PreferenceManager2.getInstance(this) }
    private val insetsController by unsafeLazy { WindowInsetsControllerCompat(launcher.window, rootView) }
    private val themeProvider by unsafeLazy { ThemeProvider.INSTANCE.get(this) }
    private val noStatusBarStateListener = object : StateManager.StateListener<LauncherState> {
        override fun onStateTransitionStart(toState: LauncherState) {
            if (toState is OverviewState) {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
        override fun onStateTransitionComplete(finalState: LauncherState) {
            if (finalState !is OverviewState) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
            }
        }
    }
    private val rememberPositionStateListener = object : StateManager.StateListener<LauncherState> {
        override fun onStateTransitionStart(toState: LauncherState) {
            if (toState is AllAppsState) {
                mAppsView.activeRecyclerView.restoreScrollPosition()
            }
        }
        override fun onStateTransitionComplete(finalState: LauncherState) {}
    }
    private val statusBarClockListener = object : StateManager.StateListener<LauncherState> {
        override fun onStateTransitionStart(toState: LauncherState) {
            when (toState) {
                is BackgroundAppState,
                is OverviewState,
                is AllAppsState,
                -> {
                    LawnchairApp.instance.restoreClockInStatusBar()
                }

                else -> {
                    workspace.updateStatusbarClock()
                }
            }
        }
        override fun onStateTransitionComplete(finalState: LauncherState) {}
    }
    private val clearSearchStateListener = object : StateManager.StateListener<LauncherState> {
        override fun onStateTransitionComplete(finalState: LauncherState) {
            if (finalState == LauncherState.NORMAL && mAppsView != null && mAppsView.isSearching) {
                mAppsView?.post {
                    mAppsView.reset(false, true)
                }
            }
        }
    }

    private lateinit var colorScheme: ColorScheme
    private var hasBackGesture = false
    private var isUserPresentReceiverRegistered = false
    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                logOverlayState("userPresent:beforeEnsure")
                defaultOverlay.ensureContentAttached()
                ensureRightScreen()
                updateRightScreenUi()
                verifyRightScreenAttachment("userPresent")
                logOverlayState("userPresent:afterEnsure")
                Toast.makeText(
                    this@LawnchairLauncher,
                    R.string.user_present_toast,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    val gestureController by unsafeLazy { GestureController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!Utilities.ATLEAST_Q) {
            enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ),
            )
        }
        layoutInflater.factory2 = LawnchairLayoutFactory(this)
        super.onCreate(savedInstanceState)

        prefs.launcherTheme.subscribeChanges(this, ::updateTheme)
        prefs.feedProvider.subscribeChanges(this, googleFeedOverlay::reconnect)
        preferenceManager2.enableFeed.get().distinctUntilChanged().onEach { enable ->
            googleFeedOverlay.setEnableFeed(enable)
        }.launchIn(scope = lifecycleScope)
        launcher.stateManager.addStateListener(clearSearchStateListener)

        if (prefs.autoLaunchRoot.get()) {
            lifecycleScope.launch {
                try {
                    RootHelperManager.INSTANCE.get(this@LawnchairLauncher)
                } catch (_: RootNotAvailableException) {
                }
            }
        }

        preferenceManager2.showStatusBar.get().distinctUntilChanged().onEach {
            with(insetsController) {
                if (it) {
                    show(WindowInsetsCompat.Type.statusBars())
                } else {
                    hide(WindowInsetsCompat.Type.statusBars())
                }
            }
            with(launcher.stateManager) {
                if (it) {
                    removeStateListener(noStatusBarStateListener)
                } else {
                    addStateListener(noStatusBarStateListener)
                }
            }
        }.launchIn(scope = lifecycleScope)

        preferenceManager2.statusBarClock.get().onEach {
            with(launcher.stateManager) {
                if (it) {
                    addStateListener(statusBarClockListener)
                } else {
                    removeStateListener(statusBarClockListener)
                    // Make sure status bar clock is restored when the preference is toggled off
                    LawnchairApp.instance.restoreClockInStatusBar()
                }
            }
        }
        preferenceManager2.rememberPosition.get().onEach {
            with(launcher.stateManager) {
                if (it) {
                    addStateListener(rememberPositionStateListener)
                } else {
                    removeStateListener(rememberPositionStateListener)
                }
            }
        }.launchIn(scope = lifecycleScope)

        prefs.overrideWindowCornerRadius.subscribeValues(this) {
            QuickStepContract.sHasCustomCornerRadius = it
        }
        prefs.windowCornerRadius.subscribeValues(this) {
            QuickStepContract.sCustomCornerRadius = it.toFloat()
        }
        preferenceManager2.roundedWidgets.onEach(launchIn = lifecycleScope) {
            RoundedCornerEnforcement.sRoundedCornerEnabled = it
        }
        val isWorkspaceDarkText = Themes.getAttrBoolean(this, R.attr.isWorkspaceDarkText)
        preferenceManager2.darkStatusBar.onEach(launchIn = lifecycleScope) { darkStatusBar ->
            systemUiController.updateUiState(UI_STATE_BASE_WINDOW, isWorkspaceDarkText || darkStatusBar)
        }
        preferenceManager2.backPressGestureHandler.onEach(launchIn = lifecycleScope) { handler ->
            hasBackGesture = handler !is GestureHandlerConfig.NoOp
        }

        LauncherOptionsPopup.restoreMissingPopupOptions(launcher)
        LauncherOptionsPopup.migrateLegacyPreferences(launcher)

        // Handle update from version 12 Alpha 4 to version 12 Alpha 5.
        if (
            prefs.themedIcons.get() &&
            packageManager.getThemedIconPacksInstalled(this).isEmpty()
        ) {
            prefs.themedIcons.set(newValue = false)
        }

        colorScheme = themeProvider.colorScheme

        showQuickstepWarningIfNecessary()

        reloadIconsIfNeeded()

        AppDatabase.INSTANCE.get(this).checkpointSync()
    }

    override fun collectStateHandlers(out: MutableList<StateHandler<LauncherState>>) {
        super.collectStateHandlers(out)
        out.add(SearchBarStateHandler(this))
    }

    override fun getSupportedShortcuts(): Stream<SystemShortcut.Factory<*>> = Stream.concat(
        super.getSupportedShortcuts(),
        Stream.concat(
            Stream.of(LawnchairShortcut.UNINSTALL, LawnchairShortcut.CUSTOMIZE),
            if (LawnchairApp.isRecentsEnabled) Stream.of(LawnchairShortcut.PAUSE_APPS) else Stream.empty(),
        ),
    )

    override fun updateTheme() {
        if (themeProvider.colorScheme != colorScheme) {
            recreate()
        } else {
            super.updateTheme()
        }
    }

    override fun createTouchControllers(): Array<TouchController> {
        val verticalSwipeController = VerticalSwipeTouchController(this, gestureController)
        return arrayOf<TouchController>(verticalSwipeController) + super.createTouchControllers()
    }

    override fun openLeftScreenWidgets() {
        OptionsPopupView.openWidgets(this)
    }

    override fun handleHomeTap() {
        gestureController.onHomePressed()
    }

    override fun registerBackDispatcher() {
        if (LawnchairApp.isAtleastT) {
            super.registerBackDispatcher()
        }
    }

    override fun bindItems(items: List<ItemInfo>, forceAnimateIcons: Boolean) {
        val inflatedItems = items.map { i ->
            Pair.create(
                i,
                itemInflater?.inflateItem(
                    i,
                    modelWriter,
                ),
            )
        }.toList()
        bindInflatedItems(inflatedItems, if (forceAnimateIcons) AnimatorSet() else null)
    }

    override fun handleGestureContract(intent: Intent?) {
        if (!LawnchairApp.isRecentsEnabled) {
            val gnc = GestureNavContract.fromIntent(intent)
            if (gnc != null) {
                AbstractFloatingView.closeOpenViews(
                    this,
                    false,
                    AbstractFloatingView.TYPE_ICON_SURFACE,
                )
                LawnchairFloatingSurfaceView.show(this, gnc)
            }
        }
    }

    override fun onUiChangedWhileSleeping() {
        if (Utilities.ATLEAST_S) {
            super.onUiChangedWhileSleeping()
        }
    }

    override fun showDefaultOptions(x: Float, y: Float) {
        val showWallpaperCarousel = "+carousel" in preferenceManager2.launcherPopupOrder.firstBlocking()

        if (showWallpaperCarousel) {
            show<LawnchairLauncher>(
                this,
                getPopupTarget(x, y),
                OptionsPopupView.getOptions(this),
            )
        } else {
            super.showDefaultOptions(x, y)
        }
    }

    private fun <T> show(
        activityContext: ActivityContext?,
        targetRect: RectF,
        items: List<OptionItem>,
        shouldAddArrow: Boolean = false,
        width: Int = 0,
    ): OptionsPopupView<T>? where T : Context?, T : ActivityContext? {
        if (activityContext == null) return null

        val isEmpty = WallpaperService.INSTANCE.get(this).getTopWallpapers().isEmpty()
        val layout = if (isEmpty) R.layout.longpress_options_menu else R.layout.wallpaper_options_popup

        val popup = activityContext.layoutInflater.inflate(layout, activityContext.dragLayer, false) as OptionsPopupView<T>
        popup.setTargetRect(targetRect)
        popup.setShouldAddArrow(shouldAddArrow)

        for (item in items) {
            val deepLayout = if (isEmpty) R.layout.system_shortcut else R.layout.wallpaper_options_popup_item

            val view = popup.inflateAndAdd<DeepShortcutView>(deepLayout, popup)
            if (width > 0) view.layoutParams.width = width
            view.iconView.setBackgroundDrawable(item.icon)
            view.bubbleText.text = item.label
            view.setOnClickListener(popup)
            view.onLongClickListener = popup
            popup.mItemMap[view] = item
        }

        popup.show()
        return popup
    }

    override fun createAppWidgetHolder(): LauncherWidgetHolder {
        val factory = LauncherWidgetHolder.HolderFactory.newFactory(this) as LawnchairWidgetHolder.LawnchairHolderFactory
        return factory.newInstance(
            this,
        ) { appWidgetId: Int ->
            workspace.removeWidget(
                appWidgetId,
            )
        }
    }

    override fun makeDefaultActivityOptions(splashScreenStyle: Int): ActivityOptionsWrapper {
        val callbacks = RunnableList()
        val options = if (Utilities.ATLEAST_Q) {
            LawnchairQuickstepCompat.activityOptionsCompat.makeCustomAnimation(
                this,
                0,
                0,
                Executors.MAIN_EXECUTOR.handler,
                null,
            ) {
                callbacks.executeAllAndDestroy()
            }
        } else {
            ActivityOptions.makeBasic()
        }
        if (Utilities.ATLEAST_T) {
            options.splashScreenStyle = splashScreenStyle
        }

        Utilities.allowBGLaunch(options)
        return ActivityOptionsWrapper(options, callbacks)
    }

    override fun getActivityLaunchOptions(v: View?, item: ItemInfo?): ActivityOptionsWrapper {
        return runCatching {
            super.getActivityLaunchOptions(v, item)
        }.getOrElse {
            getActivityLaunchOptionsDefault(v)
        }
    }

    private fun getActivityLaunchOptionsDefault(v: View?): ActivityOptionsWrapper {
        var left = 0
        var top = 0
        var width = v!!.measuredWidth
        var height = v.measuredHeight
        if (v is BubbleTextView) {
            // Launch from center of icon, not entire view
            val icon: Drawable? = v.icon
            if (icon != null) {
                val bounds = icon.bounds
                left = (width - bounds.width()) / 2
                top = v.paddingTop
                width = bounds.width()
                height = bounds.height()
            }
        }
        val options = Utilities.allowBGLaunch(
            ActivityOptions.makeClipRevealAnimation(
                v,
                left,
                top,
                width,
                height,
            ),
        )
        if (Utilities.ATLEAST_T) {
            options.splashScreenStyle = SplashScreen.SPLASH_SCREEN_STYLE_ICON
        }
        options.launchDisplayId = if (v.display != null) v.display.displayId else Display.DEFAULT_DISPLAY
        val callback = RunnableList()
        return ActivityOptionsWrapper(options, callback)
    }

    override fun onResume() {
        super.onResume()
        restartIfPending()
        dragLayer.post {
            logOverlayState("onResume:beforeEnsure")
            defaultOverlay.ensureContentAttached()
            if (canRestoreRightScreen("onResume")) {
                ensureRightScreen()
                updateRightScreenUi()
                verifyRightScreenAttachment("onResume")
            }
            logOverlayState("onResume:afterEnsure")
        }

        dragLayer.viewTreeObserver.addOnDrawListener(
            object : ViewTreeObserver.OnDrawListener {
                private var handled = false

                override fun onDraw() {
                    if (handled) {
                        return
                    }
                    handled = true

                    dragLayer.post {
                        dragLayer.viewTreeObserver.removeOnDrawListener(this)
                    }
                    depthController
                }
            },
        )
    }

    override fun onStart() {
        super.onStart()
        if (!isUserPresentReceiverRegistered) {
            registerReceiver(userPresentReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
            isUserPresentReceiverRegistered = true
        }
    }

    override fun onStop() {
        if (isUserPresentReceiverRegistered) {
            unregisterReceiver(userPresentReceiver)
            isUserPresentReceiverRegistered = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only actually closes if required, safe to call if not enabled
        SmartspacerClient.close()
    }

    override fun finishBindingItems(pagesBoundFirst: com.android.launcher3.util.IntSet?) {
        super.finishBindingItems(pagesBoundFirst)
        debugLog("finishBindingItems pagesBoundFirst=$pagesBoundFirst screenOrderBefore=${workspace.screenOrder.toConcatString()} childCount=${workspace.childCount}")
        ensureRightScreen()
        updateRightScreenUi()
    }

    override fun getDefaultOverlay(): LauncherOverlayManager = defaultOverlay

    private fun ensureRightScreen() {
        debugLog("ensureRightScreen before insert screenOrder=${workspace.screenOrder.toConcatString()} childCount=${workspace.childCount}")
        workspace.insertNewWorkspaceScreenBeforeEmptyScreen(RIGHT_SCREEN_ID)
        debugLog("ensureRightScreen after insert screenOrder=${workspace.screenOrder.toConcatString()} childCount=${workspace.childCount} pageIndex=${workspace.getPageIndexForScreenId(RIGHT_SCREEN_ID)}")
        val screen = workspace.getScreenWithId(RIGHT_SCREEN_ID) ?: return
        debugLog("ensureRightScreen screenFound id=${workspace.getCellLayoutId(screen)} rootChildren=${screen.childCount} swChildCount=${screen.shortcutsAndWidgets.childCount}")
        ensureRightScreenHost(screen)
        ensureRightScreenOverlayContainer()
        attachRightScreenFragment()
    }

    private fun canRestoreRightScreen(reason: String): Boolean {
        val canRestore = workspace.childCount > 0
        if (!canRestore) {
            debugLog("skipRightScreenRestore reason=$reason workspaceChildCount=${workspace.childCount} screenOrder=${workspace.screenOrder.toConcatString()}")
        }
        return canRestore
    }

    private fun ensureRightScreenHost(screen: CellLayout) {
        screen.setPadding(0, 0, 0, 0)

        if (screen.findViewById<View>(R.id.right_screen_keepalive) != null) {
            debugLog("ensureRightScreenHost keepAliveExists pageIndex=${workspace.getPageIndexForScreenId(RIGHT_SCREEN_ID)} rootChildren=${screen.childCount}")
            return
        }

        val keepAlive = View(this).apply {
            id = R.id.right_screen_keepalive
            alpha = 0f
            isClickable = false
            isFocusable = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        screen.addViewToCellLayout(
            keepAlive,
            -1,
            R.id.right_screen_keepalive,
            CellLayoutLayoutParams(0, 0, 1, 1),
            false,
        )
        debugLog("ensureRightScreenHost keepAliveAdded rootChildren=${screen.childCount} swChildCount=${screen.shortcutsAndWidgets.childCount}")
    }

    private fun ensureRightScreenOverlayContainer() {
        val dragLayer = findViewById<ViewGroup>(R.id.drag_layer) as? FrameLayout ?: return
        if (dragLayer.findViewById<View>(R.id.right_screen_overlay_container) != null) {
            return
        }

        val container = object : FrameLayout(this) {
            private var sendTouchToWorkspace = false
            private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
            private var downX = 0f
            private var downY = 0f

            override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
                if (visibility != View.VISIBLE) {
                    sendTouchToWorkspace = false
                    return false
                }
                when (ev.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        parent?.requestDisallowInterceptTouchEvent(true)
                        downX = ev.x
                        downY = ev.y
                        sendTouchToWorkspace = false
                        workspace.onInterceptTouchEvent(ev)
                        debugLog("rightScreenTouch intercept DOWN x=${ev.x} y=${ev.y} visible=$visibility clickable=$isClickable enabled=$isEnabled")
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = ev.x - downX
                        val dy = ev.y - downY
                        if (kotlin.math.abs(dx) > touchSlop && kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                            sendTouchToWorkspace = true
                            workspace.onInterceptTouchEvent(ev)
                            debugLog("rightScreenTouch intercept MOVE dx=$dx dy=$dy sendTouchToWorkspace=$sendTouchToWorkspace")
                            return true
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        debugLog("rightScreenTouch intercept END action=${ev.actionMasked} sendTouchToWorkspace=$sendTouchToWorkspace")
                        sendTouchToWorkspace = false
                        parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }
                return false
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    debugLog("rightScreenTouch touch DOWN x=${event.x} y=${event.y} sendTouchToWorkspace=$sendTouchToWorkspace visible=$visibility clickable=$isClickable enabled=$isEnabled")
                }
                if (sendTouchToWorkspace) {
                    val handled = workspace.onTouchEvent(event)
                    debugLog("rightScreenTouch touch FORWARD action=${event.actionMasked} handled=$handled")
                    when (event.actionMasked) {
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            sendTouchToWorkspace = false
                            parent?.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    return handled
                }
                if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                    debugLog("rightScreenTouch touch END action=${event.actionMasked} sendTouchToWorkspace=$sendTouchToWorkspace")
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
                return super.onTouchEvent(event) || true
            }
        }.apply {
            id = R.id.right_screen_overlay_container
            setBackgroundColor(Color.parseColor("#FFF4E5"))
            visibility = View.GONE
            isClickable = true
            isFocusable = true
        }
        dragLayer.addView(
            container,
            2,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        debugLog("ensureRightScreenOverlayContainer added rootChildren=${dragLayer.childCount}")
    }

    private fun attachRightScreenFragment() {
        val existing = fragmentManager.findFragmentByTag(RIGHT_SCREEN_FRAGMENT_TAG)
        val overlayContainer = findViewById<View>(R.id.right_screen_overlay_container)
        val isFragmentAttachedToCurrentContainer =
            existing?.isAdded == true &&
                existing.id == R.id.right_screen_overlay_container &&
                existing.view?.parent === overlayContainer
        debugLog("attachRightScreenFragment existing=${existing != null} attachedToCurrent=$isFragmentAttachedToCurrentContainer fragment=${existing?.javaClass?.simpleName ?: RightScreenFragment::class.java.simpleName} containerExists=${overlayContainer != null}")
        if (isFragmentAttachedToCurrentContainer) {
            return
        }
        val transaction = fragmentManager.beginTransaction()
        if (existing != null) {
            debugLog("attachRightScreenFragment removingStale existingId=${existing.id} existingViewParentMatches=${existing.view?.parent === overlayContainer}")
            transaction.remove(existing)
        }
        transaction
            .replace(
                R.id.right_screen_overlay_container,
                RightScreenFragment(),
                RIGHT_SCREEN_FRAGMENT_TAG,
            )
            .commitAllowingStateLoss()
        fragmentManager.executePendingTransactions()
        val attached = fragmentManager.findFragmentByTag(RIGHT_SCREEN_FRAGMENT_TAG)
        debugLog("attachRightScreenFragment committed attached=${attached != null} pageIndex=${workspace.getPageIndexForScreenId(RIGHT_SCREEN_ID)} screenOrder=${workspace.screenOrder.toConcatString()}")
    }

    private fun verifyRightScreenAttachment(reason: String) {
        val overlayContainer = findViewById<ViewGroup>(R.id.right_screen_overlay_container)
        val fragment = fragmentManager.findFragmentByTag(RIGHT_SCREEN_FRAGMENT_TAG)
        val attachedToCurrentContainer =
            fragment?.isAdded == true &&
                fragment.id == R.id.right_screen_overlay_container &&
                fragment.view?.parent === overlayContainer
        debugLog("verifyRightScreenAttachment reason=$reason containerExists=${overlayContainer != null} containerChildCount=${overlayContainer?.childCount} fragmentExists=${fragment != null} fragmentAdded=${fragment?.isAdded} fragmentView=${fragment?.view != null} attachedToCurrent=$attachedToCurrentContainer stateSaved=${fragmentManager.isStateSaved}")
        if (overlayContainer != null && !attachedToCurrentContainer && !fragmentManager.isStateSaved) {
            debugLog("verifyRightScreenAttachment reattaching fragment reason=$reason")
            attachRightScreenFragment()
        }
    }

    private fun logOverlayState(reason: String) {
        val overlayContainer = findViewById<ViewGroup>(R.id.right_screen_overlay_container)
        val rightFragment = fragmentManager.findFragmentByTag(RIGHT_SCREEN_FRAGMENT_TAG)
        val rightPageIndex = workspace.getPageIndexForScreenId(RIGHT_SCREEN_ID)
        debugLog("overlayState reason=$reason currentPage=${workspace.currentPage} destinationPage=${workspace.destinationPage} scrollX=${workspace.scrollX} screenOrder=${workspace.screenOrder.toConcatString()} rightPageIndex=$rightPageIndex rightContainerExists=${overlayContainer != null} rightContainerVisibility=${overlayContainer?.visibility} rightContainerAlpha=${overlayContainer?.alpha} rightContainerChildren=${overlayContainer?.childCount} rightFragmentExists=${rightFragment != null} rightFragmentAdded=${rightFragment?.isAdded} rightFragmentView=${rightFragment?.view != null} rightFragmentParentMatches=${rightFragment?.view?.parent === overlayContainer} rightVisible=${isRightScreenVisible()}")
    }

    private fun updateRightScreenUi(
        currentPage: Int = workspace.currentPage,
        destinationPage: Int = workspace.destinationPage,
    ) {
        val overlayContainer = findViewById<View>(R.id.right_screen_overlay_container) ?: return
        val rightPageIndex = workspace.getPageIndexForScreenId(RIGHT_SCREEN_ID)
        val currentScreenId = workspace.getScreenIdForPageIndex(currentPage)
        val destinationScreenId = workspace.getScreenIdForPageIndex(destinationPage)
        val progress = getRightScreenProgress(rightPageIndex)
        val isSettlingToRightScreen = destinationScreenId == RIGHT_SCREEN_ID
        val isFullyOnRightScreen = currentScreenId == RIGHT_SCREEN_ID && progress >= 0.99f
        val isVisible =
            progress > RIGHT_SCREEN_VISIBLE_THRESHOLD ||
                isSettlingToRightScreen ||
                isFullyOnRightScreen
        val translationDistance = overlayContainer.width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
        overlayContainer.visibility = if (isVisible) View.VISIBLE else View.GONE
        overlayContainer.alpha = progress
        overlayContainer.translationX = (1f - progress) * translationDistance * 0.18f
        overlayContainer.isClickable = progress > RIGHT_SCREEN_INTERACTIVE_THRESHOLD
        overlayContainer.isFocusable = progress > RIGHT_SCREEN_INTERACTIVE_THRESHOLD
        overlayContainer.isEnabled = progress > RIGHT_SCREEN_INTERACTIVE_THRESHOLD

        val hotseatAlpha = 1f - progress
        hotseat.visibility = if (progress >= 0.99f) View.INVISIBLE else View.VISIBLE
        hotseat.alpha = hotseatAlpha
        hotseat.setIconsAlpha(hotseatAlpha)
        hotseat.setQsbAlpha(hotseatAlpha)
        workspace.pageIndicator?.alpha = hotseatAlpha
        workspace.pageIndicator?.visibility = if (progress >= 0.99f) View.INVISIBLE else View.VISIBLE
        debugLog("updateRightScreenUi visible=$isVisible progress=$progress currentPage=$currentPage destinationPage=$destinationPage currentScreenId=$currentScreenId destinationScreenId=$destinationScreenId settlingToRight=$isSettlingToRightScreen fullyOnRight=$isFullyOnRightScreen overlayVisibility=${overlayContainer.visibility} clickable=${overlayContainer.isClickable} enabled=${overlayContainer.isEnabled} hotseatVisible=${hotseat.visibility == View.VISIBLE}")
    }

    private fun debugLog(message: String) {
        OverlayStateFileLogger.log(this, TAG, message)
    }

    private fun getRightScreenProgress(rightPageIndex: Int): Float {
        if (rightPageIndex <= 0) return 0f
        val previousPageIndex = rightPageIndex - 1
        val startScroll = workspace.getScrollForPage(previousPageIndex)
        val endScroll = workspace.getScrollForPage(rightPageIndex)
        if (endScroll <= startScroll) {
            return if (workspace.currentPage >= rightPageIndex) 1f else 0f
        }
        return ((workspace.scrollX - startScroll).toFloat() / (endScroll - startScroll).toFloat())
            .coerceIn(0f, 1f)
    }

    override fun onPageEndTransition() {
        super.onPageEndTransition()
        updateRightScreenUi()
    }

    override fun onWorkspaceScrollChanged(currentPage: Int, destinationPage: Int) {
        super.onWorkspaceScrollChanged(currentPage, destinationPage)
        updateRightScreenUi(currentPage, destinationPage)
    }

    fun isRightScreenVisible(): Boolean {
        return findViewById<View>(R.id.right_screen_overlay_container)?.visibility == View.VISIBLE
    }

    fun recreateIfNotScheduled() {
        if (sRestartFlags == 0) {
            recreate()
        }
    }

    private fun restartIfPending() {
        when {
            sRestartFlags and FLAG_RESTART != 0 -> lawnchairApp.restart(false)

            sRestartFlags and FLAG_RECREATE != 0 -> {
                sRestartFlags = 0
                recreate()
            }
        }
    }

    /**
     * Reloads app icons if there is an active icon pack & [PreferenceManager2.alwaysReloadIcons] is enabled.
     */
    private fun reloadIconsIfNeeded() {
        if (
            preferenceManager2.alwaysReloadIcons.firstBlocking()
        ) {
            LauncherAppState.getInstance(this).reloadIcons()
        }
    }

    companion object {
        private const val TAG = "RightScreenDebug"
        private const val RIGHT_SCREEN_VISIBLE_THRESHOLD = 0.02f
        private const val RIGHT_SCREEN_INTERACTIVE_THRESHOLD = 0.08f
        private const val FLAG_RECREATE = 1 shl 0
        private const val FLAG_RESTART = 1 shl 1
        private const val RIGHT_SCREEN_ID = 1_000_001
        private const val RIGHT_SCREEN_FRAGMENT_TAG = "right_screen_fragment"

        var sRestartFlags = 0

        val instance get() = LauncherAppState.getInstanceNoCreate()?.launcher as? LawnchairLauncher
    }
}

val Context.launcher: LawnchairLauncher
    get() = BaseActivity.fromContext(this)

val Context.launcherNullable: LawnchairLauncher? get() = try {
    launcher
} catch (_: IllegalArgumentException) {
    null
}
