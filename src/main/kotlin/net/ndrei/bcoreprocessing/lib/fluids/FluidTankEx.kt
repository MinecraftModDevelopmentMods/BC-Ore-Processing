package net.ndrei.bcoreprocessing.lib.fluids

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidTank

abstract class FluidTankEx(capacity: Int) : FluidTank(capacity), INBTSerializable<NBTTagCompound> {
    override fun serializeNBT() = this.writeToNBT(net.minecraft.nbt.NBTTagCompound())

    override fun deserializeNBT(nbt: NBTTagCompound?) {
        if (nbt != null) {
            this.readFromNBT(nbt)
        }
    }
}