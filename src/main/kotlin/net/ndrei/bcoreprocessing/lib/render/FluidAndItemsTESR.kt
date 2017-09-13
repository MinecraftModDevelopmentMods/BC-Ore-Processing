package net.ndrei.bcoreprocessing.lib.render

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity

object FluidAndItemsTESR : TileEntitySpecialRenderer<TileEntity>() {
    override fun renderTileEntityAt(te: TileEntity?, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int) {
        arrayOf(ItemStackTESR, FluidStacksTESR).forEach {
            it.setRendererDispatcher(this.rendererDispatcher)
            it.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage)
        }

        super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage)
    }
}