package app.lawnchair.bi.e

import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * event sender
 *
 * created on 2025/8/25
 * @author holmes
 */
object Eve {

    private const val TAG = "eve"

    private val backends = mutableListOf<Evend>()

    private val interceptors = CopyOnWriteArrayList<EvInterceptor>()

    fun addBackend(evend: Evend) {
        backends.add(evend)
    }

    fun addInterceptor(interceptor: EvInterceptor) {
        interceptors.addIfAbsent(interceptor)
    }

    fun removeInterceptor(interceptor: EvInterceptor) {
        interceptors.remove(interceptor)
    }

    init {
        addInterceptor(BasePropInterceptor())
    }

    fun send(ev: String, prop: Map<String, Any>) {
        val propCopy = prop.toMutableMap()

        var interrupted = false
        for (interceptor in interceptors) {
            val rr = runCatching {
                interceptor.onSend(ev, propCopy)
            }
            if (rr.isFailure) {
                Log.w(TAG, "interceptor failed: ${rr.exceptionOrNull()?.message}")
                continue
            }
            val r = rr.getOrThrow()
            if (r.isFailure) {
                Log.w(TAG, "ev break, [${ev}] (${r.exceptionOrNull()?.message})")
                interrupted = true
                break
            }
        }

        if (interrupted) {
            return
        }

        for (evend in backends) {
            runCatching {
                evend.send(ev, propCopy)
            }.onFailure {
                Log.e(TAG, "send event failed: ${it.message}")
            }
        }
    }

}
