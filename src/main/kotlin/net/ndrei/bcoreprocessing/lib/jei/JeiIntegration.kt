package net.ndrei.bcoreprocessing.lib.jei

import mezz.jei.api.IJeiRuntime
import mezz.jei.api.IModPlugin
import mezz.jei.api.IModRegistry
import mezz.jei.api.JEIPlugin
import mezz.jei.api.recipe.IRecipeCategoryRegistration
import net.minecraft.block.Block
import net.ndrei.bcoreprocessing.BCOreProcessing

@JEIPlugin
class JeiIntegration : IModPlugin {
    override fun register(registry: IModRegistry) {
        JeiIntegration.blocksMap.values.forEach { it.register(registry) }
    }

    override fun registerCategories(registry: IRecipeCategoryRegistration?) {
        if (registry != null) {
            JeiIntegration.blocksMap.values.forEach { it.register(registry) }
        }
        else
            BCOreProcessing.logger.warn("TheJeiThing::registerCategories - Null registry received.")
    }

    override fun onRuntimeAvailable(jeiRuntime: IJeiRuntime?) {
        if (jeiRuntime != null) {
            JeiIntegration.JEI = jeiRuntime
        }
        else
            BCOreProcessing.logger.warn("TheJeiThing::onRuntimeAvailable - Null runtime received.")
    }

    companion object {
        var JEI: IJeiRuntime? = null
            private set

        private val blocksMap = mutableMapOf<Block, BaseCategory<*>>()

        init {
            JeiIntegration.registerCategory(OreProcessorCategory)
            JeiIntegration.registerCategory(FluidProcessorCategory)
        }

        fun registerCategory(category: BaseCategory<*>) {
            blocksMap[category.block] = category
        }

        fun isBlockRegistered(block: Block): Boolean
            = blocksMap.containsKey(block)

        fun showCategory(block: Block) {
            if ((JEI != null) && (blocksMap.containsKey(block))) {
                JEI!!.recipesGui.showCategories(mutableListOf(
                    blocksMap[block]!!.uid
                ))
            }
        }
    }
}
