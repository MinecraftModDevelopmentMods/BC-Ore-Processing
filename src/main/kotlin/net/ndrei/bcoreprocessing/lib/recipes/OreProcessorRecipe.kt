package net.ndrei.bcoreprocessing.lib.recipes

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipe
import net.ndrei.bcoreprocessing.lib.copyWithSize
import net.ndrei.bcoreprocessing.lib.equalsIgnoreSize

class OreProcessorRecipe(private val input: ItemStack, private val outputs: Pair<FluidStack?, FluidStack?>, private val ticks: Int)
    : IOreProcessorRecipe {

    override fun isInput(stack: ItemStack, ignoreSize: Boolean) =
        this.input.equalsIgnoreSize(stack) && (ignoreSize || (this.input.count <= stack.count))
    override fun getProcessingTicks() = this.ticks
    override fun getPossibleInputs() = arrayOf(this.input.copy())
    override fun getTotalOutput() = this.outputs
        .let { Pair(it.first?.copy(), it.second?.copy()) }

    override fun processInput(stack: ItemStack): ItemStack {
        if (!this.isInput(stack, false)) {
            throw Exception("Invalid input item stack: $stack.")
        }

        val result = stack.copyWithSize(this.input.count)
        stack.shrink(this.input.count)
        return result
    }

    override fun getOutputForTick(tick: Int) =
        when (tick) {
            in Int.MIN_VALUE..-1 -> Pair(null, null)
            in this.ticks..Int.MAX_VALUE -> Pair(null, null)
            else /* in 0..this.ticks-1 */ -> this.outputs.let {
                val firstAmount = it.first.atTick(tick + 1, this.ticks) - it.first.atTick(tick, this.ticks)
                val secondAmount = it.second.atTick(tick + 1, this.ticks) - it.second.atTick(tick, this.ticks)
                return@let Pair(
                    if ((firstAmount > 0) && (it.first != null)) FluidStack(it.first!!.fluid, firstAmount) else null,
                    if ((secondAmount > 0) && (it.second != null)) FluidStack(it.second!!.fluid, secondAmount) else null
                )
            }
        }

    private fun FluidStack?.atTick(tick: Int, ticks: Int) =
        if ((this == null) || (tick == 0)) 0 else ((this.amount.toFloat() / ticks.toFloat()) * tick.toFloat()).toInt()
}
