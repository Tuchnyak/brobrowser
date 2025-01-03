package net.tuchnyak.brobrowser.view.panel

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.util.preferredWidth
import net.tuchnyak.brobrowser.icons.PluginIcons
import net.tuchnyak.brobrowser.persistent.PersistentService
import net.tuchnyak.brobrowser.persistent.UrlInfo
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Toolkit
import javax.swing.DefaultListCellRenderer
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * @author tuchnyak (George Shchennikov)
 */
object HeaderPanel {

    fun getHeader(
        project: Project,
        browserInstance: JBCefBrowser,
        mainPanel: MainPanel
    ): JPanel = initCustomPanel {
        it.layout = BorderLayout(5, 10)

        it.add(initBrowserHistoryPanel(browserInstance), BorderLayout.WEST)

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

        it.add(initButtonPanel(project, mainPanel, dropDown), BorderLayout.EAST)
    }

}

private fun initBrowserHistoryPanel(browserInstance: JBCefBrowser): JPanel = initCustomPanel {
    it.layout = FlowLayout(FlowLayout.LEFT, 0, 0)
    it.add(getBackButton(browserInstance))
    it.add(getForwardButton(browserInstance))
}

private fun getForwardButton(browserInstance: JBCefBrowser): Component {
    val btnForward = JButtonSizedWithIcon(PluginIcons.forwardButton)
    btnForward.addActionListener {
        if (browserInstance.cefBrowser.canGoForward()) {
            browserInstance.cefBrowser.goForward()
        }
    }

    return btnForward
}

private fun getBackButton(browserInstance: JBCefBrowser): Component {
    val btnBack = JButtonSizedWithIcon(PluginIcons.backButton)
    btnBack.addActionListener {
        if (browserInstance.cefBrowser.canGoBack()) {
            browserInstance.cefBrowser.goBack()
        }
    }

    return btnBack
}

private fun initButtonPanel(
    project: Project,
    mainPanel: MainPanel,
    dropDown: JComboBox<String>
): JPanel = initCustomPanel {
    it.layout = FlowLayout(FlowLayout.RIGHT, 0, 0)

    val addBtn = JButtonSizedWithIcon(PluginIcons.addIcon)
    val editBtn = JButtonSizedWithIcon(PluginIcons.editIcon)
    val deleteBtn = JButtonSizedWithIcon(PluginIcons.deleteIcon)

    addBtn.addActionListener {
        val nameInput = JTextField()
        val addressInput = JTextField()

        val addDialog = AddDialog(project, nameInput, addressInput)
        val isOk = addDialog.showAndGet()
        if (isOk) {
            PersistentService.addOrUpdateUrl(
                UrlInfo(nameInput.text, addressInput.text),
                project
            )
            PersistentService.setupLastPage(nameInput.text, project)
            mainPanel.redraw()
        }
    }
    it.add(addBtn)

    editBtn.addActionListener {
        dropDown.selectedItem?.toString()?.apply {
            val url = PersistentService.getUrlByName(project, this)
            val nameInput = JTextField(this)
            val addressInput = JTextField(url)

            val addDialog = AddDialog(project, nameInput, addressInput, "Edit bookmark")
            val isOk = addDialog.showAndGet()
            if (isOk) {
                PersistentService.removeUrl(this, project)
                PersistentService.addOrUpdateUrl(
                    UrlInfo(nameInput.text, addressInput.text),
                    project
                )
                PersistentService.setupLastPage(nameInput.text, project)
                mainPanel.redraw()
            }
        }
    }
    it.add(editBtn)

    deleteBtn.addActionListener {
        val dialog = DeleteDialog(project)
        dropDown.selectedItem?.takeIf { dialog.showAndGet() }?.toString()?.apply {
            PersistentService.removeUrl(this, project)
            PersistentService.setupLastPage(PersistentService.getNames(project).first(), project)
            mainPanel.redraw()
        }
    }
    it.add(deleteBtn)
}

private class DeleteDialog(val project: Project) : DialogWrapper(project) {

    init {
        title = "Delete bookmark?"
        isModal = true
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val p = JPanel()
        p.layout = VerticalFlowLayout(VerticalFlowLayout.CENTER)
        p.add(JLabel("Are you sure?"))

        return p
    }
}

private class AddDialog(
    val project: Project,
    val name: JTextField,
    val address: JTextField,
    val windowTitle: String = "Add new bookmark"
) : DialogWrapper(project) {

    init {
        title = windowTitle
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

private class JButtonSizedWithIcon(icon: Icon) : JButton(icon) {

    init {
        val screenResolution = Toolkit.getDefaultToolkit().screenResolution.toDouble()
        val scaleFactor = screenResolution / 96.0 // 96 DPI — базовый уровень

        val iconWidth = icon.iconWidth
        val iconHeight = icon.iconHeight

        val basePadding = 20
        val padding = (basePadding * scaleFactor).toInt()
        val buttonWidth = iconWidth + padding
        val buttonHeight = iconHeight + padding

        preferredSize = Dimension(buttonWidth, buttonHeight)
        minimumSize = preferredSize
        maximumSize = preferredSize
    }

}