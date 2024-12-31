package net.tuchnyak.brobrowser.view.panel

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import net.tuchnyak.brobrowser.persistent.PersistentService
import net.tuchnyak.brobrowser.persistent.UrlInfo
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*

/**
 * @author tuchnyak (George Shchennikov)
 */
class MainPanel(val project: Project) : JPanel()

fun MainPanel.init(): JPanel {
    this.layout = BorderLayout()

    //// BROWSER
    val browserInstance = JBCefBrowser()
    val browserPanel = initCustomPanel {
        it.layout = BorderLayout()
        if (JBCefApp.isSupported()) {
            browserInstance.loadURL(PersistentService.getLastPageInfo(project).address)
            it.add(browserInstance.component, BorderLayout.CENTER)

            browserInstance.jbCefClient.addLoadHandler(
                object : CefLoadHandlerAdapter() {
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
                },
                browserInstance.cefBrowser
            )

            browserInstance.jbCefClient.addRequestHandler(
                object : CefRequestHandlerAdapter() {
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
                                    setHeaderByName(
                                        "User-Agent",
                                        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36",
                                        true
                                    )
                                }
                                return false
                            }
                        }
                    }
                },
                browserInstance.cefBrowser
            )
        } else {
            it.add(JLabel("JBCef browser unsupported."), BorderLayout.CENTER)
        }
    }
    this.add(browserPanel)

    //// HEADER
    val headerPanel = initCustomPanel {
        it.layout = BorderLayout(5, 10)

        val btn = JButton("+")
        btn.addActionListener {
            val nameInput = JTextField()
            val addressInput = JTextField()
            val addDialog = AddDialog(project, nameInput, addressInput)
            val isOk = addDialog.showAndGet()
            if (isOk) {
                PersistentService.addOrUpdateUrl(
                    UrlInfo(nameInput.text, addressInput.text),
                    project
                )
                this.redraw()
            }
        }
        it.add(btn, BorderLayout.EAST)

        val dropDown = JComboBox<String>(PersistentService.getNames(project).sorted().toTypedArray())
        dropDown.isEditable = false
        dropDown.selectedItem = PersistentService.getLastPageInfo(project).title
        dropDown.addActionListener {
            val selected = dropDown.selectedItem?.toString()
            if (selected != null) {
                PersistentService.setupLastPage(selected, project)
                browserInstance.loadURL(PersistentService.getLastPageInfo(project).address)
            }
        }
        dropDown.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component? {
                val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (list != null && index >= 0) {
                    list.toolTipText = PersistentService.getUrlByName(project, value.toString())
                }
                return component
            }
        }
        it.add(dropDown, BorderLayout.CENTER)
    }
    this.add(headerPanel, BorderLayout.NORTH)

    return this
}

fun MainPanel.redraw() {
    this.removeAll()
    this.init()
    this.revalidate()
    this.repaint()
}

private fun initCustomPanel(codeBlock: (panel: JPanel) -> Unit): JPanel = with(JPanel()) {
    codeBlock(this)
    return this
}

private class AddDialog(project: Project, val name: JTextField, val address: JTextField) : DialogWrapper(project) {

    init {
        title = "TEST TITLE"
        isModal = true
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val p = JPanel()
        p.layout = VerticalFlowLayout(0, 5)
        p.add(JLabel("Bookmark name:"))
        p.add(name)
        p.add(JLabel("Bookmark URL:"))
        p.add(address)

        return p
    }

}