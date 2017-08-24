package net.ndrei.bcoreprocessing.lib.gui

import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import net.ndrei.bcoreprocessing.MOD_ID

enum class GuiTextures(private val path: String) {
    GUI_BASE("textures/gui/gui-base.png"),
    GUI_JEI("textures/gui/gui-jei.png");

    val resourceLocation: ResourceLocation
        get() = ResourceLocation(MOD_ID, this.path)

    fun bind(screen: GuiScreen) {
        screen.mc.textureManager.bindTexture(this.resourceLocation)
    }
}

fun GuiScreen.bindTexture(texture: GuiTextures) = texture.bind(this)
