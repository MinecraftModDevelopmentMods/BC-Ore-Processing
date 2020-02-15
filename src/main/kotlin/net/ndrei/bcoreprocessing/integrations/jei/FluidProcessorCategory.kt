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
import net.ndrei.bcoreprocessing.api.recipes.IFluidProcessorRecipe
import net.ndrei.bcoreprocessing.lib.gui.GuiTextures
import net.ndrei.bcoreprocessing.lib.recipes.FluidProcessorRecipeManager
import net.ndrei.bcoreprocessing.machines.fluidprocessor.FluidProcessorBlock

object FluidProcessorCategory
    : BaseCategory<FluidProcessorCategory.RecipeWrapper>(FluidProcessorBlock) {
    override fun drawExtras(minecraft: Minecraft?) { }
    override fun getTooltipStrings(mouseX: Int, mouseY: Int) = mutableListOf<String>()
    override fun getIcon() = null

    private lateinit var fluidOverlay: IDrawable

    override fun setRecipe(recipeLayout: IRecipeLayout, recipeWrapper: RecipeWrapper, ingredients: IIngredients) {
        val stacks = recipeLayout.itemStacks
        stacks.init(0, false, 33, 9)
        stacks.set(0, ingredients.getOutputs(ItemStack::class.java)[0])

        val capacity = arrayOf(recipeWrapper.recipe.getRecipeInput(), recipeWrapper.recipe.getRecipeOutput().second)
            .filterNotNull()
            .map { it.amount }
            .max()

        val fluids = recipeLayout.fluidStacks
        fluids.init(0, true, 5, 5, 8, 27, capacity ?: recipeWrapper.recipe.getRecipeInput().amount, capacity != null, this.fluidOverlay)
        fluids.set(0, ingredients.getInputs(FluidStack::class.java)[0])

        if (recipeWrapper.recipe.getRecipeOutput().second != null) {
            fluids.init(1, false, 55, 5, 8, 27, capacity ?: recipeWrapper.recipe.getRecipeOutput().second!!.amount, capacity != null, this.fluidOverlay)
            fluids.set(1, ingredients.getOutputs(FluidStack::class.java)[0])
        }
    }

    class RecipeWrapper(val recipe: IFluidProcessorRecipe)
        : IRecipeWrapper {
        override fun drawInfo(minecraft: Minecraft?, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int) { }
        override fun getTooltipStrings(mouseX: Int, mouseY: Int) = mutableListOf<String>()
        override fun handleClick(minecraft: Minecraft?, mouseX: Int, mouseY: Int, mouseButton: Int) = false

        override fun getIngredients(ingredients: IIngredients) {
            ingredients.setInput(FluidStack::class.java, this.recipe.getRecipeInput())
            val output = recipe.getRecipeOutput()
            ingredients.setOutput(ItemStack::class.java, output.first)
            if (output.second != null) {
                ingredients.setOutput(FluidStack::class.java, output.second!!)
            }
        }
    }

    override fun register(registry: IRecipeCategoryRegistration) {
        super.register(registry)

        this.recipeBackground = this.guiHelper.createDrawable(GuiTextures.GUI_JEI.resourceLocation, 0, 66, 124, 37)
        this.fluidOverlay = this.guiHelper.createDrawable(GuiTextures.GUI_JEI.resourceLocation, 5, 71, 8, 27)
    }

    override fun register(registry: IModRegistry) {
        super.register(registry)

        registry.handleRecipes(IFluidProcessorRecipe::class.java, { RecipeWrapper(it) }, this.uid)
        registry.addRecipes(FluidProcessorRecipeManager.allRecipes, this.uid)
    }
}
