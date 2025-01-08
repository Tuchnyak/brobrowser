package net.tuchnyak.brobrowser.view.panel

import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefApp
import net.tuchnyak.brobrowser.browser.BrowserService
import net.tuchnyak.brobrowser.persistent.PersistentService
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

private const val UNSUPPORTED_MESSAGE = "JBCef browser unsupported."

/**
 * @author tuchnyak (George Shchennikov)
 */
object BrowserPanel {

    fun getBrowser(
        project: Project,
        browserService: BrowserService
    ): JPanel = initCustomPanel {
        it.layout = BorderLayout()

        if (JBCefApp.isSupported()) {
            browserService.loadURL(PersistentService.getLastPageInfo(project).address)
            it.add(browserService.getComponent(), BorderLayout.CENTER)
            browserService.loadURL(PersistentService.getLastPageInfo(project).address)
        } else {
            it.add(JLabel(UNSUPPORTED_MESSAGE), BorderLayout.CENTER)
        }
    }


}