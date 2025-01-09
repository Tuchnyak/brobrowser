package net.tuchnyak.brobrowser.view.panel

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.VerticalFlowLayout
import net.tuchnyak.brobrowser.browser.BrowserService
import net.tuchnyak.brobrowser.browser.getBrowserService
import net.tuchnyak.brobrowser.icons.PluginIcons
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author tuchnyak (George Shchennikov)
 */
class MainPanel(val project: Project) : JPanel() {
    lateinit var browserService: BrowserService
}

fun MainPanel.init(): JPanel {
    this.layout = BorderLayout()

    try {
        this.browserService = getBrowserService()

        val headerPanel = HeaderPanel.getHeader(project, browserService, this)
        this.add(headerPanel, BorderLayout.NORTH)

        val browserPanel = BrowserPanel.getBrowser(project, browserService)
        this.add(browserPanel, BorderLayout.CENTER)
    } catch (e: Exception) {
        this.add(initCustomPanel {
            it.layout = VerticalFlowLayout(VerticalFlowLayout.CENTER, 20, 30, true, false)
            val label = JLabel("<html>Sorry, your IDE doesn't support BroBrowser: '${e.message}'</html>")
            label.horizontalAlignment = JLabel.CENTER
            it.add(label)
            it.add(JLabel(PluginIcons.errorIcon))
        })
    }

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
