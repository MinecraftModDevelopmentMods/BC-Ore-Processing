package net.ndrei.bcoreprocessing.lib.recipes

import net.minecraft.item.ItemStack
import net.minecraft.util.JsonUtils
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.BCOreProcessing
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipe
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipeManager
import net.ndrei.bcoreprocessing.lib.config.readFluidStack
import net.ndrei.bcoreprocessing.lib.config.readItemStacks

object OreProcessorRecipeManager : IOreProcessorRecipeManager {
    private val recipes = mutableListOf<IOreProcessorRecipe>()

    override fun registerRecipe(recipe: IOreProcessorRecipe) {
        this.recipes.add(recipe)
    }

    override fun createRecipe(input: ItemStack, output: Pair<FluidStack?, FluidStack?>, ticks: Int) =
        OreProcessorRecipe(input, output, ticks)

    override fun findFirstRecipe(input: ItemStack, ignoreSize: Boolean) =
        this.recipes.firstOrNull { it.isInput(input, ignoreSize) }

    fun registerRecipes() {
        BCOreProcessing.configHelper.readExtraRecipesFile("ore_processor") {
            val stacks = it.readItemStacks("input_stack")
            if (!stacks.isEmpty() && it.has("output_fluids")) {
                val ticks = JsonUtils.getInt(it, "ticks", 0)
                if (ticks > 0) {
                    val rawFluids = it.get("output_fluids")
                    if (rawFluids.isJsonObject) {
                        val fluids = Pair(
                            rawFluids.asJsonObject.readFluidStack("first"),
                            rawFluids.asJsonObject.readFluidStack("second")
                        )
                        if ((fluids.first != null) || (fluids.second != null)) {
                            stacks.forEach {
                                this.registerSimpleRecipe(it, fluids, ticks)
                            }
                        }
                    }
                }
            }
        }
    }

    val allRecipes get() = this.recipes.toList()
}
