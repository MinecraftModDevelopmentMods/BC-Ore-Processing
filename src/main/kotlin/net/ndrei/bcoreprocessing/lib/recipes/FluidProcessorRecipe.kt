package net.ndrei.bcoreprocessing.lib.recipes

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.api.recipes.IFluidProcessorRecipe

class FluidProcessorRecipe(private val input: FluidStack, private val output: ItemStack, private val residue: FluidStack?, private val ticks: Int)
    : IFluidProcessorRecipe {

    override fun isInput(stack: FluidStack, ignoreSize: Boolean) =
        (stack.fluid == this.input.fluid) && (ignoreSize || (stack.amount >= this.input.amount))

    override fun getProcessingTicks() = this.ticks
    override fun getRecipeInput() = this.input.copy()
    override fun getRecipeOutput() = Pair(this.output.copy(), this.residue?.copy())

    override fun processInput(stack: FluidStack): FluidStack? =
        if (!this.isInput(stack, true)) null
        else {
            stack.amount -= this.input.amount
            this.input.copy()
        }

    override fun getOutputForTick(tick: Int) =
        when (tick) {
            in Int.MIN_VALUE..-1 -> Pair(ItemStack.EMPTY, null)
            in this.ticks..Int.MAX_VALUE -> Pair(ItemStack.EMPTY, null)
            else -> Pair(
                if (this.ticks == (tick + 1)) this.output.copy() else ItemStack.EMPTY,
                this.residue?.let {
                    val amount = it.atTick(tick + 1, this.ticks) - it.atTick(tick, this.ticks)
                    return@let if (amount > 0) FluidStack(it.fluid, amount) else null
                }
            )
        }

    private fun FluidStack?.atTick(tick: Int, ticks: Int) =
        if ((this == null) || (tick == 0)) 0 else ((this.amount.toFloat() / ticks.toFloat()) * tick.toFloat()).toInt()

}
