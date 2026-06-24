package app.lawnchair.bi.e

/**
 *
 *
 * created on 2025/8/25
 * @author holmes
 */
interface Evend {

    fun name(): String

    fun send(ev: String, prop: Map<String, Any>)

}
