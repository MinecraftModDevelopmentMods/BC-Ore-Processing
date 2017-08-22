package net.ndrei.bcoreprocessing.lib.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity

object ItemStackTESR : TileEntitySpecialRenderer<TileEntity>() {
    override fun render(te: TileEntity?, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
        val stack = (te as? IItemStackHolder)?.getItemStack() ?: ItemStack.EMPTY
        if ((te == null) || stack.isEmpty) {
            return;
        }

        this.rendererDispatcher.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false)
        this.rendererDispatcher.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)

        GlStateManager.enableRescaleNormal()
        // GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.enableBlend()
        // RenderHelper.enableStandardItemLighting()
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
        GlStateManager.pushMatrix()
        val ibakedmodel = Minecraft.getMinecraft().renderItem.getItemModelWithOverrides(stack, te.world, null)

        GlStateManager.translate(x + .5f, y + .25f, z + .5f)
        GlStateManager.scale(1.5f, 1.5f, 1.5f)
        GlStateManager.color(1.0f, 1.0f, 1.0f, .75f)

        val transformedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false)
        Minecraft.getMinecraft().renderItem.renderItem(stack, transformedModel)

        GlStateManager.popMatrix()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        this.rendererDispatcher.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap()
    }
}
