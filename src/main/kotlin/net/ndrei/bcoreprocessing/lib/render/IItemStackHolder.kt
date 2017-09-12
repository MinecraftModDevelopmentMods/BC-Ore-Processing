package net.ndrei.bcoreprocessing.lib.render

import net.minecraft.item.ItemStack

interface IItemStackHolder {
    var renderAngle: Float
    fun getItemStack(): ItemStack
}
