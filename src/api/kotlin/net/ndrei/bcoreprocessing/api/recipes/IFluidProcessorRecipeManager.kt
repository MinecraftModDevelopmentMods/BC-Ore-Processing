package net.ndrei.bcoreprocessing.api.recipes

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

interface IFluidProcessorRecipeManager {
    fun registerRecipe(recipe: IFluidProcessorRecipe)

    fun createRecipe(input: FluidStack, output: ItemStack, residue: FluidStack?, ticks: Int): IFluidProcessorRecipe

    fun registerSimpleRecipe(input: FluidStack, output: ItemStack, residue: FluidStack?, ticks: Int) {
        this.registerRecipe(this.createRecipe(input, output, residue, ticks))
    }

    fun findFirstRecipe(input: FluidStack, ignoreSize: Boolean): IFluidProcessorRecipe?
}
