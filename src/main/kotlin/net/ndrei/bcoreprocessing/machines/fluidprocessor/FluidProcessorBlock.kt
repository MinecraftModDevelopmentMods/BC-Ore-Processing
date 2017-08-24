package net.ndrei.bcoreprocessing.machines.fluidprocessor

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockRenderLayer
import net.ndrei.bcoreprocessing.BCOreProcessing
import net.ndrei.bcoreprocessing.MOD_ID
import net.ndrei.teslacorelib.annotations.AutoRegisterBlock
import net.ndrei.teslacorelib.blocks.OrientedBlock

@AutoRegisterBlock
object FluidProcessorBlock
    : OrientedBlock<FluidProcessorTile>(MOD_ID, BCOreProcessing.creativeTab, "fluid_processor", FluidProcessorTile::class.java) {

    override fun getBlockLayer() = BlockRenderLayer.TRANSLUCENT
    override fun isOpaqueCube(state: IBlockState?) = false
}
