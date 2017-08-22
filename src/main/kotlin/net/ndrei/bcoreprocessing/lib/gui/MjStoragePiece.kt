package net.ndrei.bcoreprocessing.lib.gui

import buildcraft.api.mj.MjAPI
import buildcraft.api.mj.MjBattery
import com.google.common.collect.Lists
import com.mojang.realmsclient.gui.ChatFormatting
import net.ndrei.teslacorelib.gui.BasicContainerGuiPiece
import net.ndrei.teslacorelib.gui.BasicTeslaGuiContainer

class MjStoragePiece(left: Int, top: Int, private val battery: MjBattery)
    : BasicContainerGuiPiece(left, top, WIDTH, HEIGHT) {

    override fun drawBackgroundLayer(container: BasicTeslaGuiContainer<*>, guiX: Int, guiY: Int, partialTicks: Float, mouseX: Int, mouseY: Int) {
        GuiTextures.GUI_BASE.bind(container)

        container.drawTexturedRect(this.left, this.top, 1, 189, this.width, this.height)

        val powerPercent = this.battery.stored.toFloat() / this.battery.capacity.toFloat()
        val power = (Math.min(1.0f, powerPercent) * (this.height - 6)).toInt()

        container.drawTexturedRect(this.left + 2, this.top + 2, 20, 191, this.width - 4, this.height - 4)
        container.drawTexturedRect(this.left + 3, this.top + 3 + this.height - 6 - power, 35, 192 + this.height - 6 - power, this.width - 6, power + 2)
    }

    override fun drawForegroundTopLayer(container: BasicTeslaGuiContainer<*>, guiX: Int, guiY: Int, mouseX: Int, mouseY: Int) {
        if (super.isInside(container, mouseX, mouseY)) {
            val lines = Lists.newArrayList<String>()
            lines.add("${ChatFormatting.DARK_PURPLE}Stored Energy")
            lines.add("${ChatFormatting.AQUA}${MjAPI.formatMj(this.battery.stored)} MJ ${ChatFormatting.DARK_GRAY}of")
            lines.add("${ChatFormatting.RESET}${MjAPI.formatMj(this.battery.capacity)} MJ")

            container.drawTooltip(lines, mouseX - guiX, mouseY - guiY)
        }
    }

    companion object {
        const val WIDTH = 18
        const val HEIGHT = 54
    }
}
