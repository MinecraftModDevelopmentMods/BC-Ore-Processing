package net.ndrei.bcoreprocessing.lib.gui

import net.minecraft.inventory.Container
import net.ndrei.bcoreprocessing.machines.BaseMJMachine
import net.ndrei.teslacorelib.gui.BasicTeslaGuiContainer

class BaseMJGuiContainer<out T : BaseMJMachine>(guiId: Int, container: Container, entity: T)
    : BasicTeslaGuiContainer<T>(guiId, container, entity) {

    override val containerWidth get() = 175
    override val containerHeight get() = 167

    override fun drawGuiContainerBackground() {
        GuiTextures.GUI_BASE.bind(this)
        this.drawTexturedModalRect(super.guiLeft, super.guiTop, 0, 0, super.getXSize(), super.getYSize())
    }
}
