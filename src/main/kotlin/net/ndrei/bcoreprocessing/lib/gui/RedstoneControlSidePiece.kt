package net.ndrei.bcoreprocessing.lib.gui

import net.ndrei.bcoreprocessing.machines.BaseMJMachine
import net.ndrei.teslacorelib.gui.BasicTeslaGuiContainer
import net.ndrei.teslacorelib.gui.SideDrawerPiece
import net.ndrei.teslacorelib.inventory.BoundingRectangle
import net.ndrei.teslacorelib.inventory.IRedstoneControlledMachine

class RedstoneControlSidePiece(private val entity: BaseMJMachine, topIndex: Int) : SideDrawerPiece(topIndex) {
    override val currentState get() = this.entity.redstoneControl.ordinal

    override fun getStateToolTip(state: Int) =
        listOf(
            when(this.entity.redstoneControl) {
                IRedstoneControlledMachine.RedstoneControl.RedstoneOn -> "Active with redstone signal"
                IRedstoneControlledMachine.RedstoneControl.RedstoneOff -> "Active without redstone signal"
                IRedstoneControlledMachine.RedstoneControl.AlwaysActive -> "Always active"
            }
        )

    override fun renderState(container: BasicTeslaGuiContainer<*>, state: Int, box: BoundingRectangle) {
        GuiTextures.GUI_BASE.bind(container)
        container.drawTexturedRect(box.left - container.guiLeft + 3, box.top - container.guiTop + 2, 147 + state * 18, 229, 12, 12)
    }

    override fun clicked() {
        this.entity.toggleRedstoneControl()
    }
}
