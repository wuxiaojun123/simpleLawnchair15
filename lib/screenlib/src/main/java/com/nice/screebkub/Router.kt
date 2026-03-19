package com.nice.screebkub

import com.didi.drouter.api.DRouter

object Router {
    fun <T> getSrv(serviceClass: Class<T>): T? {
        return runCatching {
            @Suppress("UNCHECKED_CAST")
            DRouter.build(serviceClass).getService() as? T
        }.getOrNull()
    }
}
