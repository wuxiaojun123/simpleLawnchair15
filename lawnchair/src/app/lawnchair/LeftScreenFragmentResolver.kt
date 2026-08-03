package app.lawnchair

import androidx.fragment.app.Fragment
import com.nice.screebkub.OverlayStateFileLogger
import com.simplepdf.pdfeditor.fragment.HomeFragment

object LeftScreenFragmentResolver {
    private const val TAG = "LeftScreenResolver"

    fun createFragment(launcher: LawnchairLauncher): Fragment {
        val resolved = HomeFragment()
        OverlayStateFileLogger.log(
            launcher,
            TAG,
            "createFragment resolved=${resolved.javaClass.name}",
        )
        return resolved
    }
}
