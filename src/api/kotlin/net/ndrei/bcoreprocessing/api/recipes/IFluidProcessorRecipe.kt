package net.ndrei.bcoreprocessing.api.recipes

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

interface IFluidProcessorRecipe {
    /**
     * Tests if a FluidStack can be used as an input for this recipe.
     *
     * @param stack the possible input FluidStack
     * @param ignoreSize if true, only test the fluid and not also if there is an enough amount of it for the recipe
     * @return 'true' if the FluidStack is a possible input
     */
    fun isInput(stack: FluidStack, ignoreSize: Boolean): Boolean

    /**
     * Modifies the input FluidStack by taking out the amount required by this recipe
     *
     * @param stack the input fluid stack to be modified
     * @return the fluid taken from the input stack (or a totally different FluidStack, used for display purposes only)
     */
    fun processInput(stack: FluidStack): FluidStack?

    /**
     * Returns the amounts of items and fluid generated for the current tick
     *
     * @param tick the current tick in processing this recipe (range 0 .. processingTicks)
     * @return generated item and fluids.
     */
    fun getOutputForTick(tick: Int): Pair<ItemStack, FluidStack?>

    /**
     * Gets the total number of ticks this recipe needs to be processed for
     */
    fun getProcessingTicks(): Int

    /**
     * Gets this recipe's input fluid.
     * This is only used for display purposes and JEI integration.
     */
    fun getRecipeInput(): FluidStack

    /**
     * Gets all the outputs of this recipe.
     * This is only used for display purposes and JEI integration
     */
    fun getRecipeOutput(): Pair<ItemStack, FluidStack?>
}
