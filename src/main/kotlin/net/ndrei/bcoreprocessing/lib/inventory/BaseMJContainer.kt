package net.ndrei.bcoreprocessing.lib.inventory

import net.minecraft.entity.player.EntityPlayer
import net.ndrei.bcoreprocessing.machines.BaseMJMachine
import net.ndrei.teslacorelib.containers.BasicTeslaContainer

class BaseMJContainer<T: BaseMJMachine>(entity: T, player: EntityPlayer?)
    : BasicTeslaContainer<T>(entity, player) {

    override val showPlayerExtraSlots get() = false

    override val inventoryOffsetY get() = 85
    override val inventoryQuickBarOffsetY get() = 143
}
