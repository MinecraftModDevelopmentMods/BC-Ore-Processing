package net.ndrei.bcoreprocessing.lib.render

import net.minecraft.item.ItemStack

interface IItemStackHolder {
    fun getItemStack(): ItemStack
}
