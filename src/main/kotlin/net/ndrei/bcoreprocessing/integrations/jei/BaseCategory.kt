package net.ndrei.bcoreprocessing.integrations.jei

import mezz.jei.api.IGuiHelper
import mezz.jei.api.IModRegistry
import mezz.jei.api.gui.IDrawable
import mezz.jei.api.recipe.IRecipeCategory
import mezz.jei.api.recipe.IRecipeCategoryRegistration
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.ndrei.bcoreprocessing.MOD_NAME

abstract class BaseCategory<T: IRecipeWrapper>(val block: Block) : IRecipeCategory<T> {
    protected lateinit var guiHelper: IGuiHelper
    protected lateinit var recipeBackground: IDrawable
    // protected lateinit var slotBackground: IDrawable

    override fun getModName() = MOD_NAME

    override fun getUid(): String {
        return this.block.registryName?.resourcePath ?: this.block.javaClass.canonicalName
    }

    override fun getTitle(): String {
        return this.block.localizedName
    }

    override fun getBackground(): IDrawable {
        return this.recipeBackground
    }

    open fun register(registry: IRecipeCategoryRegistration) {
        registry.addRecipeCategories(this)

        this.guiHelper = registry.jeiHelpers.guiHelper
        // this.slotBackground = this.guiHelper.createDrawable(Textures.MACHINES_TEXTURES.resource, 6, 6, 18, 18)
    }

    open fun register(registry: IModRegistry) {
        registry.addRecipeCatalyst(ItemStack(this.block), this.uid)
    }
}
