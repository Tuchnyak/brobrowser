package net.tuchnyak.brobrowser.persistent

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

/**
 * @author tuchnyak (George Shchennikov)
 */
class PluginState : BaseState() {
    var urlMap by linkedMap<String, String>()
}

@Service(Service.Level.PROJECT)
@State(
    name = "BroBrowser",
    storages = [Storage("brobrowser_data_state.xml")],
    reloadable = true
)
class PersistentService : SimplePersistentStateComponent<PluginState>(PluginState()) {

    companion object {
        fun getPluginStateInstance(project: Project): PluginState = project.getService<PersistentService>(PersistentService::class.java).state

        fun addUrl(url: UrlInfo, project: Project) {
            getPluginStateInstance(project).addUrl(url.title, url.address)
        }

        fun removeUrl(url: UrlInfo, project: Project) {
            getPluginStateInstance(project).removeUrl(url.title)
        }
    }

}

class UrlInfo(val title: String, val address: String)


private fun PluginState.addUrl(name: String, url: String) {
    urlMap = urlMap.copyAndPut(name, url)
}

private fun PluginState.removeUrl(name: String) {
    urlMap = urlMap.copyAndRemove(name)
}

private fun <K : Any, V : Any> MutableMap<K, V>.copyAndPut(key: K, value: V): MutableMap<K, V> {
    val tmpMap = mutableMapOf<K, V>();
    tmpMap.putAll(this)
    tmpMap.put(key, value)

    return tmpMap
}

private fun <K : Any, V : Any> MutableMap<K, V>.copyAndRemove(key: K): MutableMap<K, V> {
    val tmpMap = mutableMapOf<K, V>();
    tmpMap.putAll(this)
    tmpMap.remove(key)

    return tmpMap
}
