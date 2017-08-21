package net.ndrei.bcoreprocessing.api.recipes

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

/**
 * Represents a OreProcessor recipe.
 */
interface IOreProcessorRecipe {
    /**
     * Tests if an ItemStack can be used as an input for this recipe.
     *
     * @param stack the possible input ItemStack
     * @return 'true' if the ItemStack is a possible input
     */
    fun isInput(stack: ItemStack): Boolean

    /**
     * Modifies the input ItemStack by taking out the items required by this recipe
     *
     * @param stack the input stack to be modified
     * @return the items taken from the item stack (or a totally different ItemStack, used for display purposes only)
     */
    fun processInput(stack: ItemStack): ItemStack

    /**
     * Returns the amounts of fluid generated for the current tick
     *
     * @param tick the current tick in processing this recipe (range 0 .. processingTicks)
     * @return generated fluids. Only the first 2 fluids will be handled. If there are more than 2 fluids the rest will be ignored.
     */
    fun getOutputForTick(tick: Int): Array<FluidStack>

    /**
     * Gets the total number of ticks this recipe needs to be processed for
     */
    fun getProcessingTicks(): Int

    /**
     * Gets a list of possible inputs.
     * This is only used for display purposes and JEI integration.
     */
    fun getPossibleInputs(): Array<ItemStack>

    /**
     * Gets a list of all the outputs of this recipe.
     * This is only used for display purposes and JEI integration
     */
    fun getTotalOutput(): Array<FluidStack>
}
