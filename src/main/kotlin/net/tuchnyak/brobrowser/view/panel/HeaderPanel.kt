package net.tuchnyak.brobrowser.view.panel

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.jcef.JBCefBrowser
import net.tuchnyak.brobrowser.persistent.PersistentService
import net.tuchnyak.brobrowser.persistent.UrlInfo
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.DefaultListCellRenderer
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
                mainPanel.redraw()
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

}

private class AddDialog(project: Project, val name: JTextField, val address: JTextField) : DialogWrapper(project) {

    init {
        title = "Add new bookmark"
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
