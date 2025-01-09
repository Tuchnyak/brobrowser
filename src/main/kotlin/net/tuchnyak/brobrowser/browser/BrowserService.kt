package net.tuchnyak.brobrowser.browser

import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import net.tuchnyak.brobrowser.persistent.PersistentService
import net.tuchnyak.brobrowser.persistent.UrlInfo
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.*
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import javax.swing.JComponent

/**
 * @author tuchnyak (George Shchennikov)
 */
interface BrowserService {
    val userAgentHeaderName: String
        get() = "User-Agent"
    val userAgentValue: String
        get() = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"

    fun loadURL(address: String)

    fun canGoForward(): Boolean
    fun goForward()

    fun canGoBack(): Boolean
    fun goBack()

    fun getComponent(): JComponent
}

fun getBrowserService(project: Project): BrowserService {
    try {
        return getJbCefBrowserServiceImpl(project)
    } catch(e: Exception) {
        throw IllegalStateException("Not a JetBrainsProduct!: ${e.message}")
    }
}

private fun getJbCefBrowserServiceImpl(project: Project): BrowserService = object : BrowserService {

    val jbCefBrowserInstance: JBCefBrowser = JBCefBrowser()

    init {
        jbCefBrowserInstance.jbCefClient.addRequestHandler(getRequestHandler(), jbCefBrowserInstance.cefBrowser)
        jbCefBrowserInstance.jbCefClient.addLoadHandler(getLoadHandler(project), jbCefBrowserInstance.cefBrowser)
    }

    override fun loadURL(address: String) {
        jbCefBrowserInstance.loadURL(address)
    }

    override fun canGoForward(): Boolean {
        return jbCefBrowserInstance.cefBrowser.canGoForward()
    }

    override fun goForward() {
        jbCefBrowserInstance.cefBrowser.goForward()
    }

    override fun canGoBack(): Boolean {
        return jbCefBrowserInstance.cefBrowser.canGoBack()
    }

    override fun goBack() {
        jbCefBrowserInstance.cefBrowser.goBack()
    }

    override fun getComponent(): JComponent {
        return jbCefBrowserInstance.component
    }

    private fun getLoadHandler(project: Project): CefLoadHandler {
        return object : CefLoadHandlerAdapter() {
            override fun onLoadingStateChange(
                browser: CefBrowser?,
                isLoading: Boolean,
                canGoBack: Boolean,
                canGoForward: Boolean
            ) {
                browser?.url
                    ?.takeIf { it.isNotBlank() and !isLoading }
                    ?.let { newUrl ->
                        PersistentService.addOrUpdateUrl(
                            UrlInfo(
                                PersistentService.getLastPageInfo(project).title,
                                newUrl
                            ),
                            project
                        )
                    }
            }
        }
    }

    private fun getRequestHandler(): CefRequestHandler {
        return object : CefRequestHandlerAdapter() {
            override fun getResourceRequestHandler(
                browser: CefBrowser?,
                frame: CefFrame?,
                request: CefRequest?,
                isNavigation: Boolean,
                isDownload: Boolean,
                requestInitiator: String?,
                disableDefaultHandling: BoolRef?
            ): CefResourceRequestHandler? {
                return object : CefResourceRequestHandlerAdapter() {
                    override fun onBeforeResourceLoad(
                        browser: CefBrowser?,
                        frame: CefFrame?,
                        request: CefRequest?
                    ): Boolean {
                        request?.apply {
                            setHeaderByName(userAgentHeaderName, userAgentValue, true)
                        }
                        return false
                    }
                }
            }
        }
    }

}
