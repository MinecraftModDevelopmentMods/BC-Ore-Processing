package net.ndrei.bcoreprocessing.integrations.jei

import mezz.jei.api.*
import mezz.jei.api.recipe.IRecipeCategoryRegistration

@JEIPlugin
class JeiIntegration : IModPlugin {
    override fun register(registry: IModRegistry) {
        OreProcessorCategory.register(registry)
        FluidProcessorCategory.register(registry)
    }

    override fun registerCategories(registry: IRecipeCategoryRegistration) {
        OreProcessorCategory.register(registry)
        FluidProcessorCategory.register(registry)
    }

    override fun onRuntimeAvailable(jeiRuntime: IJeiRuntime) {
        JeiIntegration.JEI = jeiRuntime
    }

    companion object {
        lateinit var JEI: IJeiRuntime
            private set
    }
}
