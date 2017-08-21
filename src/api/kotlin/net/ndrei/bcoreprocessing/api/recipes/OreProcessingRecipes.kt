package net.ndrei.bcoreprocessing.api.recipes

object OreProcessingRecipes {
    private var set = false
    lateinit var oreProcessorRecipes : IOreProcessorRecipeManager private set

    fun init(oreProcessorRecipes: IOreProcessorRecipeManager) {
        if (set) {
            // ignore the evil mod trying to do this?
            return
        }
        set = true

        OreProcessingRecipes.oreProcessorRecipes = oreProcessorRecipes
    }
}
