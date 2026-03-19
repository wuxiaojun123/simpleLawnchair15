package com.nice.library_news

import android.app.Fragment
import com.didi.drouter.annotation.Service
import com.nice.screebkub.ILeftScreenService

@Service(function = arrayOf(ILeftScreenService::class))
class NewsLeftScreenService : ILeftScreenService {

    override fun getFragment(): Fragment {
        return NewsLeftScreenFragment()
    }

}
