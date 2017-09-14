package net.ndrei.bcoreprocessing.lib.recipes

import net.minecraft.item.ItemStack
import net.minecraft.util.JsonUtils
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.oredict.OreDictionary
import net.ndrei.bcoreprocessing.BCOreProcessing
import net.ndrei.bcoreprocessing.api.recipes.IFluidProcessorRecipe
import net.ndrei.bcoreprocessing.api.recipes.IFluidProcessorRecipeManager
import net.ndrei.bcoreprocessing.lib.config.readFluidStack
import net.ndrei.bcoreprocessing.lib.config.readItemStack
import net.ndrei.bcoreprocessing.lib.copyWithSize
import net.ndrei.bcoreprocessing.lib.fluids.FluidTemperature
import net.ndrei.bcoreprocessing.lib.fluids.FluidsRegistry

object FluidProcessorRecipeManager : IFluidProcessorRecipeManager {
    private val recipes = mutableListOf<IFluidProcessorRecipe>()

    override fun registerRecipe(recipe: IFluidProcessorRecipe) {
        this.recipes.add(recipe)
    }

    override fun createRecipe(input: FluidStack, output: ItemStack, residue: FluidStack?, ticks: Int) =
        FluidProcessorRecipe(input, output, residue, ticks)

    override fun findFirstRecipe(input: FluidStack, ignoreSize: Boolean) =
        this.recipes.firstOrNull { it.isInput(input, ignoreSize) }

    fun registerRecipes() {
        BCOreProcessing.configHelper.readExtraRecipesFile("fluid_processor") {
            val input = it.readFluidStack("input_fluid") ?: return@readExtraRecipesFile
            val output = it.readItemStack("output_stack") ?: return@readExtraRecipesFile
            val residue = it.readFluidStack("residue")
            val ticks = JsonUtils.getInt(it, "ticks", 0)
            if (ticks <= 0) return@readExtraRecipesFile

            this.registerSimpleRecipe(input, output, residue, ticks)

            if (it.has("variants")) {
                val temperature = FluidTemperature.values().firstOrNull {
                    input.fluid.name.endsWith("-${it.name}", true)
                }
                if (temperature != null) {
                    val rawVariants = it.get("variants")
                    if (rawVariants.isJsonArray) {
                        rawVariants.asJsonArray.forEach {
                            if (!it.isJsonObject) return@forEach
                            val rawTemperature = JsonUtils.getString(it.asJsonObject, "temperature", "")
                            val newTemperature = (if (rawTemperature.isNullOrBlank()) null else FluidTemperature.values().firstOrNull {
                                rawTemperature.equals(it.name, true)
                            }) ?: return@forEach

                            val fluidName = input.fluid.name.removeSuffix(temperature.name.toLowerCase()) + newTemperature.name.toLowerCase()
                            val newFluid = if (FluidRegistry.isFluidRegistered(fluidName)) FluidRegistry.getFluid(fluidName) else return@forEach

                            val newOutputQuantity = JsonUtils.getInt(it.asJsonObject, "items", output.count)
                            val newResidueQuantity = if (residue != null) JsonUtils.getInt(it.asJsonObject, "residue", residue.amount) else 0

                            this.registerSimpleRecipe(
                                FluidStack(newFluid, input.amount),
                                output.copyWithSize(newOutputQuantity),
                                residue?.copy()?.also { it.amount = newResidueQuantity },
                                ticks)
                        }
                    }
                }
            }
        }


        FluidsRegistry.getFluidToProcess().forEach {
            val ores = OreDictionary.getOres(it.oreName)
            if (ores.isNotEmpty()) {
                val ingot = OreDictionary.getOres(it.ingotName).firstOrNull() ?: return@forEach
                FluidTemperature.values().reversed()
                    .map { temperature ->
                        val fluid = FluidRegistry.getFluid("bcop-${it.fluidName}-${temperature.name.toLowerCase()}") ?: return@map
                        this.registerSimpleRecipe(
                            FluidStack(fluid, 1000),
                            ingot.copyWithSize(temperature.baseIngots * it.multiplier),
                            FluidStack(FluidsRegistry.GASEOUS_LAVA[2], temperature.baseResidue),
                            40)
                    }
            }
        }
    }

    val allRecipes get() = this.recipes.toList()
}
