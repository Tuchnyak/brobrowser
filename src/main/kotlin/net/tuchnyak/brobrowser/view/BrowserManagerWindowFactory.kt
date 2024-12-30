package net.tuchnyak.brobrowser.view

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import net.tuchnyak.brobrowser.view.panel.MainPanel
import net.tuchnyak.brobrowser.view.panel.init

/**
 * @author tuchnyak (George Shchennikov)
 */
class BrowserManagerWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        invokeLater {
            val mainPanel = MainPanel(project)
            val content = ContentFactory.getInstance().createContent(mainPanel.init(), "Browser", false)
            toolWindow.contentManager.addContent(content)
        }
    }

}