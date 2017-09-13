package net.ndrei.bcoreprocessing.lib.recipes

import net.minecraft.item.ItemStack
import net.minecraft.util.JsonUtils
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.oredict.OreDictionary
import net.ndrei.bcoreprocessing.BCOreProcessing
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipe
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipeManager
import net.ndrei.bcoreprocessing.lib.config.readFluidStack
import net.ndrei.bcoreprocessing.lib.config.readItemStacks
import net.ndrei.bcoreprocessing.lib.fluids.FluidsRegistry

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

        FluidsRegistry.getFluidToProcess().forEach {
            val fluid = FluidRegistry.getFluid("bcop-${it.fluidName}-searing") ?: return@forEach
            val ores = OreDictionary.getOres(it.oreName)
            if (ores.isNotEmpty()) {
                /*val ingot = */OreDictionary.getOres(it.ingotName).firstOrNull() ?: return@forEach

                ores.forEach {
                    this.registerSimpleRecipe(it, Pair(FluidStack(fluid, 1000), FluidStack(FluidsRegistry.GASEOUS_LAVA[3], 125)), 40)
                }
            }
        }
    }

    val allRecipes get() = this.recipes.toList()
}
