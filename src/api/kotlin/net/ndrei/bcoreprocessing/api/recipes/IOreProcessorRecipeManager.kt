package net.ndrei.bcoreprocessing.api.recipes

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

interface IOreProcessorRecipeManager {
    fun registerRecipe(recipe: IOreProcessorRecipe)

    fun createRecipe(input: ItemStack, output: Array<FluidStack>, ticks: Int): IOreProcessorRecipe

    fun registerSimpleRecipe(input: ItemStack, output: Array<FluidStack>, ticks: Int) {
        this.registerRecipe(this.createRecipe(input, output, ticks))
    }

    fun findFirstRecipe(input: ItemStack): IOreProcessorRecipe?
}
