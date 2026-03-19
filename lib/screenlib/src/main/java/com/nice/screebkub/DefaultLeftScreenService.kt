package com.nice.screebkub

import android.app.Fragment
import com.didi.drouter.annotation.Service

//@Service(function = arrayOf(ILeftScreenService::class))
class DefaultLeftScreenService : ILeftScreenService {

    override fun getFragment(): Fragment {
        return LeftScreenFragment()
    }

}
