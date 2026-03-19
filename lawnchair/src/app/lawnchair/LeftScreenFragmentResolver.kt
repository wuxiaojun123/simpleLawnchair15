package app.lawnchair

import android.app.Fragment
import com.nice.library_news.NewsLeftScreenFragment
import com.nice.screebkub.ILeftScreenService
import com.nice.screebkub.OverlayStateFileLogger
import com.nice.screebkub.Router
import com.nice.screebkub.LeftScreenFragment

object LeftScreenFragmentResolver {
    private const val TAG = "LeftScreenResolver"

    fun createFragment(launcher: LawnchairLauncher): Fragment {
        val service = Router.getSrv(ILeftScreenService::class.java)
        if (service == null) {
            OverlayStateFileLogger.log(launcher, TAG, "getSrv returned null, fallback to default")
        }

        val fragment = runCatching {
            service?.getFragment()
        }.onFailure {
            OverlayStateFileLogger.log(launcher, TAG, "getFragment failed error=${it.javaClass.simpleName}")
        }.getOrNull()

        val resolved = fragment ?: NewsLeftScreenFragment()
        OverlayStateFileLogger.log(
            launcher,
            TAG,
            "createFragment serviceExists=${service != null} resolved=${resolved.javaClass.name}",
        )
        return resolved
    }
}
