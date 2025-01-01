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
    var lastTitle by string()
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

        fun addOrUpdateUrl(url: UrlInfo, project: Project) {
            getPluginStateInstance(project).addOrUpdateUrl(url.title, url.address)
        }

        fun removeUrl(key: String, project: Project) {
            getPluginStateInstance(project).removeUrl(key)
        }

        fun setupLastPage(name: String, project: Project) {
            getPluginStateInstance(project).lastTitle = name
        }

        fun getLastPageInfo(project: Project): UrlInfo {
            val pluginState = getPluginStateInstance(project)
            val address = pluginState.urlMap[pluginState.lastTitle]

            return UrlInfo(pluginState.lastTitle ?: "none", address ?: ":blank")
        }

        fun getNames(project: Project): Set<String> {
            return getPluginStateInstance(project).urlMap.keys.toSet()
        }

        fun getUrlByName(project: Project, name: String): String {
            return getPluginStateInstance(project).urlMap[name] ?: ""
        }

    }

}

class UrlInfo(val title: String, val address: String)


private fun PluginState.addOrUpdateUrl(name: String, url: String) {
    urlMap = urlMap.copyAndPut(name, url)
}

private fun PluginState.removeUrl(name: String) {
    urlMap = urlMap.copyAndRemove(name)
}

private fun <K : Any, V : Any> MutableMap<K, V>.copyAndPut(key: K, value: V): MutableMap<K, V> {
    val tmpMap = mutableMapOf<K, V>();
    tmpMap.putAll(this)
    tmpMap[key] = value

    return tmpMap
}

private fun <K : Any, V : Any> MutableMap<K, V>.copyAndRemove(key: K): MutableMap<K, V> {
    val tmpMap = mutableMapOf<K, V>();
    tmpMap.putAll(this)
    tmpMap.remove(key)

    return tmpMap
}
