package net.ndrei.bcoreprocessing.api.recipes

object OreProcessingRecipes {
    private var set = false
    lateinit var oreProcessorRecipes : IOreProcessorRecipeManager private set
    lateinit var fluidProcessorRecipes: IFluidProcessorRecipeManager private set

    fun init(oreProcessorRecipes: IOreProcessorRecipeManager, fluidProcessorRecipes: IFluidProcessorRecipeManager) {
        if (OreProcessingRecipes.set) {
            // ignore the evil mod trying to do this?
            return
        }
        OreProcessingRecipes.set = true

        OreProcessingRecipes.oreProcessorRecipes = oreProcessorRecipes
        OreProcessingRecipes.fluidProcessorRecipes = fluidProcessorRecipes
    }
}
