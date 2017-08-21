package net.ndrei.bcoreprocessing.lib.render

import net.minecraftforge.fluids.FluidStack

interface IFluidStacksHolder {
    fun getFluidStacks(): Array<FluidStack>
    fun getTotalCapacity(): Int
}
