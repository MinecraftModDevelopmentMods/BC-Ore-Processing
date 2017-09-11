package net.ndrei.bcoreprocessing.integrations.jei

import mezz.jei.api.IModRegistry
import mezz.jei.api.gui.IDrawable
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.IRecipeCategoryRegistration
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipe
import net.ndrei.bcoreprocessing.lib.gui.GuiTextures
import net.ndrei.bcoreprocessing.lib.recipes.OreProcessorRecipeManager
import net.ndrei.bcoreprocessing.machines.oreprocessor.OreProcessorBlock

object OreProcessorCategory
    : BaseCategory<OreProcessorCategory.RecipeWrapper>(OreProcessorBlock) {
    override fun drawExtras(minecraft: Minecraft?) { }
    override fun getTooltipStrings(mouseX: Int, mouseY: Int) = mutableListOf<String>()
    override fun getIcon() = null

    private lateinit var fluidOverlay: IDrawable

    override fun setRecipe(recipeLayout: IRecipeLayout, recipeWrapper: RecipeWrapper, ingredients: IIngredients) {
        val stacks = recipeLayout.itemStacks
        stacks.init(0, true, 6, 9)
        stacks.set(0, ingredients.getInputs(ItemStack::class.java)[0])

        val fluids = recipeLayout.fluidStacks
        val outputs = recipeWrapper.recipe.getTotalOutput()
        val capacity = arrayOf(outputs.first, outputs.second).filterNotNull().map { it.amount }.max()
        if (outputs.first != null) {
            fluids.init(0, true, 45, 5, 8, 27, capacity ?: outputs.first!!.amount, capacity != null, this.fluidOverlay)
            fluids.set(0, outputs.first)
        }

        if (outputs.second != null) {
            fluids.init(1, true, 57, 5, 8, 27, capacity ?: outputs.second!!.amount, capacity != null, this.fluidOverlay)
            fluids.set(1, outputs.second)
        }
    }

    class RecipeWrapper(val recipe: IOreProcessorRecipe)
        : IRecipeWrapper {
        override fun drawInfo(minecraft: Minecraft?, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int) { }
        override fun getTooltipStrings(mouseX: Int, mouseY: Int) = mutableListOf<String>()
        override fun handleClick(minecraft: Minecraft?, mouseX: Int, mouseY: Int, mouseButton: Int) = false

        override fun getIngredients(ingredients: IIngredients) {
            ingredients.setInput(ItemStack::class.java, this.recipe.getPossibleInputs().first())
            val pair = this.recipe.getTotalOutput()
            ingredients.setOutputs(FluidStack::class.java, arrayOf(pair.first, pair.second).filterNotNull())
        }
    }

    override fun register(registry: IRecipeCategoryRegistration) {
        super.register(registry)

        this.recipeBackground = this.guiHelper.createDrawable(GuiTextures.GUI_JEI.resourceLocation, 0, 0, 124, 37)
        this.fluidOverlay = this.guiHelper.createDrawable(GuiTextures.GUI_JEI.resourceLocation, 45, 5, 8, 27)
    }

    override fun register(registry: IModRegistry) {
        super.register(registry)

        registry.handleRecipes(IOreProcessorRecipe::class.java, { RecipeWrapper(it) }, this.uid)
        registry.addRecipes(OreProcessorRecipeManager.allRecipes, this.uid)
    }
}
