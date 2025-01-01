package net.tuchnyak.brobrowser.view.panel

import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import net.tuchnyak.brobrowser.persistent.PersistentService
import net.tuchnyak.brobrowser.persistent.UrlInfo
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefRequestHandler
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

private const val UNSUPPORTED_MESSAGE = "JBCef browser unsupported."
private const val USER_AGENT = "User-Agent"
private const val USER_AGENT_VAL = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"

/**
 * @author tuchnyak (George Shchennikov)
 */
object BrowserPanel {

    fun getBrowser(
        project: Project,
        browserInstance: JBCefBrowser
    ): JPanel = initCustomPanel {
        it.layout = BorderLayout()

        if (JBCefApp.isSupported()) {
            browserInstance.loadURL(PersistentService.getLastPageInfo(project).address)
            it.add(browserInstance.component, BorderLayout.CENTER)

            browserInstance.jbCefClient.addLoadHandler(getLoadHandler(project), browserInstance.cefBrowser)
            browserInstance.jbCefClient.addRequestHandler(getRequestHandler(), browserInstance.cefBrowser)

            browserInstance.loadURL(PersistentService.getLastPageInfo(project).address)
        } else {
            it.add(JLabel(UNSUPPORTED_MESSAGE), BorderLayout.CENTER)
        }
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
                            setHeaderByName(USER_AGENT, USER_AGENT_VAL, true)
                        }
                        return false
                    }
                }
            }
        }
    }

}