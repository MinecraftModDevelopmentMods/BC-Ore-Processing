package net.ndrei.bcoreprocessing.lib.recipes

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipe
import net.ndrei.teslacorelib.utils.copyWithSize
import net.ndrei.teslacorelib.utils.equalsIgnoreSize

class OreProcessorRecipe(private val input: ItemStack, private val outputs: Array<FluidStack>, private val ticks: Int): IOreProcessorRecipe {
    override fun isInput(stack: ItemStack, ignoreSize: Boolean) =
        this.input.equalsIgnoreSize(stack) && (ignoreSize || (this.input.count <= stack.count))
    override fun getProcessingTicks() = this.ticks
    override fun getPossibleInputs() = arrayOf(this.input.copy())
    override fun getTotalOutput() = this.outputs
        .map { it.copy() }
        .toTypedArray()

    override fun processInput(stack: ItemStack): ItemStack {
        if (!this.isInput(stack, false)) {
            throw Exception("Invalid input item stack: $stack.")
        }

        val result = stack.copyWithSize(this.input.count)
        stack.shrink(this.input.count)
        return result
    }

    override fun getOutputForTick(tick: Int): Array<FluidStack> {
        return when (tick) {
            in Int.MIN_VALUE..-1 -> arrayOf()
            in this.ticks..Int.MAX_VALUE -> arrayOf()
            else /* in 0..this.ticks-1 */ -> this.outputs.mapNotNull {
                val amount = it.atTick(tick + 1, this.ticks) - it.atTick(tick, this.ticks)
                return@mapNotNull if (amount > 0) FluidStack(it.fluid, amount) else null
            }.toTypedArray()
        }
    }

    private fun FluidStack.atTick(tick: Int, ticks: Int) =
        if (tick == 0) 0 else ((this.amount.toFloat() / ticks.toFloat()) * tick.toFloat()).toInt()
}
