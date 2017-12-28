package net.ndrei.bcoreprocessing.lib.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity

object ItemStackTESR : TileEntitySpecialRenderer<TileEntity>() {
    override fun renderTileEntityAt(te: TileEntity?, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int) {
        val holder = (te as? IItemStackHolder) ?: return
        val stack = holder.getItemStack()
        if ((te == null) || (stack.isEmpty)) {
            return
        }

        this.rendererDispatcher.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false)
        this.rendererDispatcher.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)

        GlStateManager.enableRescaleNormal()
        // GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.enableBlend()
//        RenderHelper.enableStandardItemLighting()
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
        GlStateManager.pushMatrix()
        GlStateManager.disableCull()
        val ibakedmodel = Minecraft.getMinecraft().renderItem.getItemModelWithOverrides(stack, te.world, null)

        GlStateManager.translate(x + .5f, y + .25f, z + .5f)
        GlStateManager.scale(.9f, .9f, .9f)
        GlStateManager.rotate(holder.renderAngle, 0.0f, 1.0f, 0.0f)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        val transformedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false)
        Minecraft.getMinecraft().renderItem.renderItem(stack, transformedModel)

        GlStateManager.popMatrix()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        // RenderHelper.disableStandardItemLighting()
        GlStateManager.enableCull()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        this.rendererDispatcher.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap()
    }
}
