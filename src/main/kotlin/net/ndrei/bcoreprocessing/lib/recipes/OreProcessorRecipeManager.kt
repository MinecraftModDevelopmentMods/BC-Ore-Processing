package net.ndrei.bcoreprocessing.lib.recipes

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.JsonUtils
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.common.discovery.ASMDataTable
import net.minecraftforge.registries.IForgeRegistry
import net.ndrei.bcoreprocessing.BCOreProcessing
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipe
import net.ndrei.bcoreprocessing.api.recipes.IOreProcessorRecipeManager
import net.ndrei.teslacorelib.annotations.IRegistryHandler
import net.ndrei.teslacorelib.annotations.RegistryHandler
import net.ndrei.teslacorelib.config.readFluidStack
import net.ndrei.teslacorelib.config.readItemStacks

@RegistryHandler
object OreProcessorRecipeManager : IOreProcessorRecipeManager, IRegistryHandler {
    private val recipes = mutableListOf<IOreProcessorRecipe>()

    override fun registerRecipe(recipe: IOreProcessorRecipe) {
        this.recipes.add(recipe)
    }

    override fun createRecipe(input: ItemStack, output: Pair<FluidStack?, FluidStack?>, ticks: Int) =
        OreProcessorRecipe(input, output, ticks)

    override fun findFirstRecipe(input: ItemStack, ignoreSize: Boolean) =
        this.recipes.firstOrNull { it.isInput(input, ignoreSize) }

    override fun registerRecipes(asm: ASMDataTable, registry: IForgeRegistry<IRecipe>) {
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
}
