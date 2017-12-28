package net.ndrei.bcoreprocessing.machines.oreprocessor

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.crafting.CraftingManager
import net.minecraftforge.oredict.ShapedOreRecipe
import net.ndrei.bcoreprocessing.machines.BaseOreProcessorBlock

object OreProcessorBlock
    : BaseOreProcessorBlock<OreProcessorTile>("ore_processor", OreProcessorTile::class.java) {
    override fun registerRecipe() {
        CraftingManager.getInstance().addRecipe(ShapedOreRecipe(this,
            " t ",
            "gfg",
            " t ",
            't', Block.getBlockFromName("buildcraftfactory:tank"),
            'g', "gearIron",
            'f', Blocks.FURNACE))
    }
}
