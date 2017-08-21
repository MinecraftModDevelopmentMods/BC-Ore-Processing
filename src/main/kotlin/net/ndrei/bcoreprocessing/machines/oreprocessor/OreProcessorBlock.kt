package net.ndrei.bcoreprocessing.machines.oreprocessor

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockRenderLayer
import net.ndrei.bcoreprocessing.BCOreProcessing
import net.ndrei.bcoreprocessing.MOD_ID
import net.ndrei.teslacorelib.annotations.AutoRegisterBlock
import net.ndrei.teslacorelib.blocks.OrientedBlock

@AutoRegisterBlock
object OreProcessorBlock
    : OrientedBlock<OreProcessorTile>(MOD_ID, BCOreProcessing.creativeTab, "ore_processor", OreProcessorTile::class.java) {
    override fun getBlockLayer() = BlockRenderLayer.TRANSLUCENT
    override fun isOpaqueCube(state: IBlockState?) = false
}
