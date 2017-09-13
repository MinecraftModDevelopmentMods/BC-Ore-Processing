package net.ndrei.bcoreprocessing.lib.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.Fluid
import org.lwjgl.opengl.GL11

object FluidStacksTESR : TileEntitySpecialRenderer<TileEntity>() {
    override fun render(te: TileEntity?, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
        val holder = (te as? IFluidStacksHolder) ?: return
        val stacks = holder.getFluidStacks()
        if (stacks.isEmpty()) {
            return
        }

        val h1 = stacks[0].amount.toFloat() / holder.getTotalCapacity().toFloat()
        val h2 = if (stacks.size > 1) stacks[1].amount.toFloat() / holder.getTotalCapacity().toFloat() else 0.0f

        val b1 = if (stacks[0].fluid.isGaseous) 1.0f - h1 - (if ((h2 > 0.0f) && stacks[1].fluid.isGaseous) h2 else 0.0f) else 0.0f
        val b2 = if (h2 > 0.0f) (if (stacks[1].fluid.isGaseous) 1.0f - h2 else (if (stacks[0].fluid.isGaseous) 0.0f else h1)) else 0.0f

//        GlStateManager.pushAttrib()
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat() + 0.0f, y.toFloat() + 0.0f, z.toFloat() + 0.0f)
        val magicNumber = 1.0f / 16.0f
        GlStateManager.scale(magicNumber, magicNumber, magicNumber)
        super.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        GlStateManager.enableBlend()
        GlStateManager.disableCull()
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
        GlStateManager.disableLighting()
        super.setLightmapDisabled(true)
        
        if (h1 > 0.0f) {
            this.renderFluid(b1 * 15.0f + 0.5f, h1 * 15.0f, stacks[0].fluid)
        }
        if (h2 > 0.0f) {
            this.renderFluid(b2 * 15.0f + 0.5f, h2 * 15.0f, stacks[1].fluid)
        }

        super.setLightmapDisabled(false)
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.enableCull()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
//        GlStateManager.popAttrib()
    }

    private fun renderFluid(bottom: Float, height: Float, fluid: Fluid) {
        val color = fluid.color
        val still = fluid.still
//        val flowing = fluid.getFlowing()
        val stillSprite: TextureAtlasSprite =
            (if (still == null) null else Minecraft.getMinecraft().textureMapBlocks.getTextureExtry(still.toString()))
                ?: Minecraft.getMinecraft().textureMapBlocks.missingSprite

        val vertexBuffer = Tessellator.getInstance().buffer
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)

        vertexBuffer.pos(0.1, bottom + 0.1, 0.1).tex(stillSprite.maxU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(0.1, bottom + height - 0.1, 0.1).tex(stillSprite.maxU.toDouble(), stillSprite.minV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + height - 0.1, 0.1).tex(stillSprite.minU.toDouble(), stillSprite.minV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + 0.1, 0.1).tex(stillSprite.minU.toDouble(), stillSprite.maxV.toDouble()).endVertex()

        vertexBuffer.pos(0.1, bottom + 0.1, 15.9).tex(stillSprite.maxU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + 0.1, 15.9).tex(stillSprite.minU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + height - 0.1, 15.9).tex(stillSprite.minU.toDouble(), stillSprite.minV.toDouble()).endVertex()
        vertexBuffer.pos(0.1, bottom + height - 0.1, 15.9).tex(stillSprite.maxU.toDouble(), stillSprite.minV.toDouble()).endVertex()

        vertexBuffer.pos(0.1, bottom + 0.1, 0.1).tex(stillSprite.minU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(0.1, bottom + 0.1, 15.9).tex(stillSprite.maxU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(0.1, bottom + height - 0.1, 15.9).tex(stillSprite.maxU.toDouble(), stillSprite.minV.toDouble()).endVertex()
        vertexBuffer.pos(0.1, bottom + height - 0.1, 0.1).tex(stillSprite.minU.toDouble(), stillSprite.minV.toDouble()).endVertex()

        vertexBuffer.pos(15.9, bottom + 0.1, 0.1).tex(stillSprite.minU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + height - 0.1, 0.1).tex(stillSprite.minU.toDouble(), stillSprite.minV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + height - 0.1, 15.9).tex(stillSprite.maxU.toDouble(), stillSprite.minV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + 0.1, 15.9).tex(stillSprite.maxU.toDouble(), stillSprite.maxV.toDouble()).endVertex()

        vertexBuffer.pos(0.1, bottom + height - 0.1, 0.1).tex(stillSprite.minU.toDouble(), stillSprite.minV.toDouble()).endVertex()
        vertexBuffer.pos(0.1, bottom + height - 0.1, 15.9).tex(stillSprite.minU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + height - 0.1, 15.9).tex(stillSprite.maxU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + height - 0.1, 0.1).tex(stillSprite.maxU.toDouble(), stillSprite.minV.toDouble()).endVertex()

        vertexBuffer.pos(15.9, bottom + 0.1, 0.1).tex(stillSprite.maxU.toDouble(), stillSprite.minV.toDouble()).endVertex()
        vertexBuffer.pos(15.9, bottom + 0.1, 15.9).tex(stillSprite.maxU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(0.1, bottom + 0.1, 15.9).tex(stillSprite.minU.toDouble(), stillSprite.maxV.toDouble()).endVertex()
        vertexBuffer.pos(0.1, bottom + 0.1, 0.1).tex(stillSprite.minU.toDouble(), stillSprite.minV.toDouble()).endVertex()

        GlStateManager.color((color shr 16 and 0xFF) / 255.0f, (color shr 8 and 0xFF) / 255.0f, (color and 0xFF) / 255.0f, (color ushr 24 and 0xFF) / 255.0f)

        Tessellator.getInstance().draw()
    }
}
