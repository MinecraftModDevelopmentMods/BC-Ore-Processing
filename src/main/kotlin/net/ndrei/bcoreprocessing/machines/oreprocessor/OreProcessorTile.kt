package net.ndrei.bcoreprocessing.machines.oreprocessor

import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.fluids.FluidsRegistry
import net.ndrei.bcoreprocessing.lib.render.FluidStacksTESR
import net.ndrei.bcoreprocessing.lib.render.IFluidStacksHolder
import net.ndrei.bcoreprocessing.lib.render.IItemStackHolder
import net.ndrei.bcoreprocessing.lib.render.ItemStackTESR
import net.ndrei.teslacorelib.tileentities.SidedTileEntity

class OreProcessorTile : SidedTileEntity(OreProcessorTile::class.java.name.hashCode()), IItemStackHolder, IFluidStacksHolder {
    override fun getRenderers() = super.getRenderers().also {
        it.add(ItemStackTESR)
        it.add(FluidStacksTESR)
    }

    override fun getItemStack() = ItemStack(Blocks.IRON_ORE)

    override fun getFluidStacks() =
        arrayOf(
            FluidStack(FluidRegistry.getFluid("bcop-iron-cool"), 7000),
            FluidStack(FluidsRegistry.GASEOUS_LAVA[0], 4000)
        )

    override fun getTotalCapacity() = 15000

    override fun innerUpdate() {
    }
}
