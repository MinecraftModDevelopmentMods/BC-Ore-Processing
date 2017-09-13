package net.ndrei.bcoreprocessing.lib

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.ItemStackHandler

fun ItemStack.copyWithSize(size: Int) = ItemHandlerHelper.copyStackWithSize(this, size)

fun ItemStack.equalsIgnoreSize(other: ItemStack) =
    if (this.isEmpty && other.isEmpty)
        true
    else if (this.isEmpty != other.isEmpty)
        false
    else {
        val x = this.copyWithSize(other.count)
        ItemStack.areItemStacksEqualUsingNBTShareTag(x, other)
    }

fun ItemStackHandler.insertItems(stack: ItemStack, simulate: Boolean) =
    (0 until this.slots).fold(stack) { result, slot -> this.insertItem(slot, result, simulate) }

fun INBTSerializable<NBTTagCompound>.deserialize(root: NBTTagCompound, key: String) {
    if (root.hasKey(key, Constants.NBT.TAG_COMPOUND)) {
        this.deserializeNBT(root.getCompoundTag(key))
    }
}

fun INBTSerializable<NBTTagCompound>.serialize(root: NBTTagCompound, key: String) {
    root.setTag(key, this.serializeNBT())
}

fun FluidStack.copyWithSize(amount: Int) =
    FluidStack(this.fluid, amount)