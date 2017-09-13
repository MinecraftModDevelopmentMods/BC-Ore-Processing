package net.ndrei.bcoreprocessing.machines.fluidprocessor

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.crafting.CraftingManager
import net.minecraftforge.oredict.ShapedOreRecipe
import net.ndrei.bcoreprocessing.machines.BaseOreProcessorBlock

object FluidProcessorBlock
    : BaseOreProcessorBlock<FluidProcessorTile>("fluid_processor", FluidProcessorTile::class.java) {
    override fun registerRecipe() {
        CraftingManager.getInstance().addRecipe(ShapedOreRecipe(this,
            "xtx",
            "gfg",
            "xtx",
            't', Block.getBlockFromName("buildcraftfactory:tank"),
            'g', "gearIron",
            'f', Blocks.FURNACE,
            'x', "blockGlass"))

    }
}

