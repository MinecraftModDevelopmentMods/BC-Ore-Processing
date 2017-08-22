package net.ndrei.bcoreprocessing.machines

import buildcraft.api.mj.IMjConnector
import buildcraft.api.mj.IMjReceiver
import buildcraft.api.mj.MjAPI
import buildcraft.api.mj.MjBattery
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants
import net.ndrei.bcoreprocessing.lib.gui.BaseMJGuiContainer
import net.ndrei.bcoreprocessing.lib.gui.MjStoragePiece
import net.ndrei.bcoreprocessing.lib.gui.RedstoneControlSidePiece
import net.ndrei.bcoreprocessing.lib.inventory.BaseMJContainer
import net.ndrei.teslacorelib.containers.BasicTeslaContainer
import net.ndrei.teslacorelib.gui.BasicTeslaGuiContainer
import net.ndrei.teslacorelib.gui.PlayerInventoryBackground
import net.ndrei.teslacorelib.inventory.IRedstoneControlledMachine
import net.ndrei.teslacorelib.tileentities.SidedTileEntity

abstract class BaseMJMachine(entityTypeId: Int)
    : SidedTileEntity(entityTypeId) {

    protected val battery = object: MjBattery(MjAPI.ONE_MINECRAFT_JOULE * 1024) {
        private var lastPower = -1L

        override fun tick(world: World?, position: BlockPos?) {
            super.tick(world, position)

            if (this.stored != this.lastPower) {
                this.lastPower = this.stored
                this@BaseMJMachine.markDirty()
                this@BaseMJMachine.forceSync()
            }
        }
    }

    private val batteryReceiver = object: IMjReceiver {
        override fun canConnect(p0: IMjConnector) = true

        override fun getPowerRequested() =
            if (this@BaseMJMachine.battery.isFull)
                0L
            else
                this@BaseMJMachine.battery.capacity - this@BaseMJMachine.battery.stored

        override fun receivePower(p0: Long, p1: Boolean) =
            this@BaseMJMachine.battery.addPower(p0, p1)
    }

    init {
        super.redstoneControl = IRedstoneControlledMachine.RedstoneControl.RedstoneOn
    }

    //#region override default tesla entity gui

    override final fun getContainer(id: Int, player: EntityPlayer): BasicTeslaContainer<*> {
        return BaseMJContainer(this, player)
    }

    override final fun getGuiContainer(id: Int, player: EntityPlayer): BasicTeslaGuiContainer<*> {
        return BaseMJGuiContainer(id, this.getContainer(id, player), this)
    }

    override final fun supportsAddons() = false
    override final val showPauseDrawerPiece = false
    override final val showSideConfiguratorPiece = false
    override final val showRedstoneControlPiece = false

    override fun getGuiContainerPieces(container: BasicTeslaGuiContainer<*>) =
         super.getGuiContainerPieces(container).also {
             it.removeIf { it is PlayerInventoryBackground }

             it.add(RedstoneControlSidePiece(this, 0))
             it.add(MjStoragePiece(7, 25, this.battery))
         }

    override fun shouldAddFluidItemsInventory() = false

    //#endregion

    //#region capabilities & storage

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) =
        (this.canReceiveEnergyOnSide(facing) && ((capability == MjAPI.CAP_RECEIVER) || (capability == MjAPI.CAP_CONNECTOR)))
            || super.hasCapability(capability, facing)

    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        if (this.canReceiveEnergyOnSide(facing)) {
            if (capability == MjAPI.CAP_CONNECTOR) {
                return MjAPI.CAP_CONNECTOR.cast(this.batteryReceiver)
            }
            else if (capability == MjAPI.CAP_RECEIVER) {
                return MjAPI.CAP_RECEIVER.cast(this.batteryReceiver)
            }
        }
        return super.getCapability(capability, facing)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)

        if (compound.hasKey("mj_battery", Constants.NBT.TAG_COMPOUND)) {
            this.battery.deserializeNBT(compound.getCompoundTag("mj_battery"))
        }
    }

    override fun writeToNBT(compound: NBTTagCompound) =
        super.writeToNBT(compound).also {
            it.setTag("mj_battery", this.battery.serializeNBT())
        }

    open fun canReceiveEnergyOnSide(side: EnumFacing?) = true

    //#endregion

    override fun processImmediateInventories() {
        super.processImmediateInventories()

        this.battery.tick(this.world, this.pos)
    }
}
