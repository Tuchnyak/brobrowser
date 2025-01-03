package net.tuchnyak.brobrowser.icons

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.util.IconUtil
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.ImageIcon

private const val WIDTH = 20
private const val HEIGHT = WIDTH

object PluginIcons {

    @JvmField
    val toolWindow = getIconByBrightness("toolWindowIcon")

    @JvmField
    val backButton = getIconByBrightnessWithResize("btn_browser_back", WIDTH, HEIGHT)

    @JvmField
    val forwardButton = getIconByBrightnessWithResize("btn_browser_forward", WIDTH, HEIGHT)

    @JvmField
    val addIcon = getIconByBrightnessWithResize("btn_add", WIDTH, HEIGHT)

    @JvmField
    val editIcon = getIconByBrightnessWithResize("btn_edit", WIDTH, HEIGHT)

    @JvmField
    val deleteIcon = getIconByBrightnessWithResize("btn_delete", WIDTH, HEIGHT)

    private fun getIconByBrightnessWithResize(name: String, width: Int, height: Int): Icon {
        val originalSizedIcon = getIconByBrightness(name)
        val origImage = IconUtil.toBufferedImage(originalSizedIcon)
        val scaledImage = origImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH)

        return ImageIcon(scaledImage)
    }

    private fun getIconByBrightness(name: String): Icon {
       return if (JBColor.isBright()) {
           IconLoader.getIcon("/icons/$name.svg", javaClass)
       } else {
           IconLoader.getIcon("/icons/${name}_dark.svg", javaClass)
       }
    }

}