package net.ndrei.bcoreprocessing.lib.recipes

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipe
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipeManager

object OreProcessorRecipeManager : IOreProcessorRecipeManager {
    private val recipes = mutableListOf<IOreProcessorRecipe>()

    override fun registerRecipe(recipe: IOreProcessorRecipe) {
        this.recipes.add(recipe)
    }

    override fun createRecipe(input: ItemStack, output: Array<FluidStack>, ticks: Int) =
        OreProcessorRecipe(input, output, ticks)

    override fun findFirstRecipe(input: ItemStack) =
        this.recipes.firstOrNull { it.isInput(input) }
}
