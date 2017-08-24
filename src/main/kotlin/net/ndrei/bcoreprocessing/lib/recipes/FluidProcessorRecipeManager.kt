package net.ndrei.bcoreprocessing.lib.recipes

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.JsonUtils
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.common.discovery.ASMDataTable
import net.minecraftforge.registries.IForgeRegistry
import net.ndrei.bcoreprocessing.BCOreProcessing
import net.ndrei.bcoreprocessing.api.recipes.IFluidProcessorRecipe
import net.ndrei.bcoreprocessing.api.recipes.IFluidProcessorRecipeManager
import net.ndrei.bcoreprocessing.lib.fluids.FluidTemperature
import net.ndrei.teslacorelib.annotations.IRegistryHandler
import net.ndrei.teslacorelib.annotations.RegistryHandler
import net.ndrei.teslacorelib.config.readFluidStack
import net.ndrei.teslacorelib.config.readItemStack
import net.ndrei.teslacorelib.utils.copyWithSize

@RegistryHandler
object FluidProcessorRecipeManager : IFluidProcessorRecipeManager, IRegistryHandler {
    private val recipes = mutableListOf<IFluidProcessorRecipe>()

    override fun registerRecipe(recipe: IFluidProcessorRecipe) {
        this.recipes.add(recipe)
    }

    override fun createRecipe(input: FluidStack, output: ItemStack, residue: FluidStack?, ticks: Int) =
        FluidProcessorRecipe(input, output, residue, ticks)

    override fun findFirstRecipe(input: FluidStack, ignoreSize: Boolean) =
        this.recipes.firstOrNull { it.isInput(input, ignoreSize) }

    override fun registerRecipes(asm: ASMDataTable, registry: IForgeRegistry<IRecipe>) {
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
                                if (residue != null) residue.copy().also { it.amount = newResidueQuantity } else null,
                                ticks)
                        }
                    }
                }
            }
        }
    }

    val allRecipes get() = this.recipes.toList()
}
