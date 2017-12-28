package net.ndrei.bcoreprocessing.lib.render

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity

object FluidAndItemsTESR : TileEntitySpecialRenderer<TileEntity>() {
    override fun render(te: TileEntity?, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
        arrayOf(ItemStackTESR, FluidStacksTESR).forEach {
            it.setRendererDispatcher(this.rendererDispatcher)
            it.render(te, x, y, z, partialTicks, destroyStage, alpha)
        }

        super.render(te, x, y, z, partialTicks, destroyStage, alpha)
    }
}