package net.tuchnyak.brobrowser.view.panel

import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * @author tuchnyak (George Shchennikov)
 */
class MainPanel(val project: Project) : JPanel() {
    val browserInstance: JBCefBrowser = JBCefBrowser()
}

fun MainPanel.init(): JPanel {
    this.layout = BorderLayout()

    val headerPanel = HeaderPanel.getHeader(project, browserInstance, this)
    this.add(headerPanel, BorderLayout.NORTH)

    val browserPanel = BrowserPanel.getBrowser(project, browserInstance)
    this.add(browserPanel)

    return this
}

fun MainPanel.redraw() {
    this.removeAll()
    this.init()
    this.revalidate()
    this.repaint()
}

fun initCustomPanel(codeBlock: (panel: JPanel) -> Unit): JPanel = with(JPanel()) {
    codeBlock(this)
    return this
}
