package net.ndrei.bcoreprocessing.machines

import buildcraft.api.mj.IMjConnector
import buildcraft.api.mj.IMjReceiver
import buildcraft.api.mj.MjAPI
import buildcraft.api.mj.MjBattery
import buildcraft.lib.misc.MessageUtil
import buildcraft.lib.net.IPayloadReceiver
import buildcraft.lib.net.IPayloadWriter
import buildcraft.lib.net.MessageUpdateTile
import buildcraft.lib.net.PacketBufferBC
import io.netty.buffer.Unpooled
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.ItemStackHandler
import net.ndrei.bcoreprocessing.BCOreProcessing
import net.ndrei.bcoreprocessing.lib.deserialize
import net.ndrei.bcoreprocessing.lib.fluids.FluidTankEx
import net.ndrei.bcoreprocessing.lib.render.IFluidStacksHolder
import net.ndrei.bcoreprocessing.lib.render.IItemStackHolder
import net.ndrei.bcoreprocessing.lib.serialize

abstract class BaseOreProcessorMachine
    : TileEntity(), ITickable, IItemStackHolder, IFluidStacksHolder, IPayloadReceiver {

    protected val battery = object: MjBattery(MjAPI.ONE_MINECRAFT_JOULE * 1024) {
        private var lastPower = -1L

        override fun tick(world: World?, position: BlockPos?) {
            super.tick(world, position)

            if (this.stored != this.lastPower) {
                this.lastPower = this.stored
                this@BaseOreProcessorMachine.markForUpdate(STORAGE_BATTERY)
            }
        }
    }

    private val batteryReceiver = object: IMjReceiver {
        override fun canConnect(p0: IMjConnector) = true

        override fun getPowerRequested() =
            if (this@BaseOreProcessorMachine.battery.isFull)
                0L
            else
                this@BaseOreProcessorMachine.battery.capacity - this@BaseOreProcessorMachine.battery.stored

        override fun receivePower(p0: Long, p1: Boolean) =
            this@BaseOreProcessorMachine.battery.addPower(p0, p1)
    }

    protected val itemHandler = object : ItemStackHandler(1) {
        override fun extractItem(slot: Int, amount: Int, simulate: Boolean) =
            if (this@BaseOreProcessorMachine.canExtractItem(this.getStackInSlot(slot)))
                super.extractItem(slot, amount, simulate)
            else ItemStack.EMPTY

        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean) =
            if (this@BaseOreProcessorMachine.canInsertItem(stack))
                super.insertItem(slot, stack, simulate)
            else stack

        override fun getStackLimit(slot: Int, stack: ItemStack) =
            this@BaseOreProcessorMachine.getItemStackLimit(stack)

        override fun onContentsChanged(slot: Int) {
            super.onContentsChanged(slot)
            this@BaseOreProcessorMachine.markForUpdate(STORAGE_ITEM_STACK)
        }
    }

    protected val fluidTank = object : FluidTankEx(9000) {
        override fun canFillFluidType(fluid: FluidStack?) =
            (fluid != null) && this@BaseOreProcessorMachine.canFillFluidType(fluid)

        override fun canDrainFluidType(fluid: FluidStack?) =
            (fluid != null) && this@BaseOreProcessorMachine.canDrainFluidType(fluid)

        override fun onContentsChanged() {
            super.onContentsChanged()
            this@BaseOreProcessorMachine.markForUpdate(STORAGE_FLUID_TANK)
        }
    }

    protected val residueTank = object : FluidTankEx(6000) {
        override fun canFillFluidType(fluid: FluidStack?) = false

        override fun onContentsChanged() {
            super.onContentsChanged()
            this@BaseOreProcessorMachine.markForUpdate(STORAGE_RESIDUE_TANK)
        }
    }

    private var syncEverything = false
    private val syncWriters = mutableMapOf<String, IPayloadWriter>()
    private val syncReceivers = mutableMapOf<String, IPayloadReceiver>()
    private val syncOnNextTick = mutableSetOf<String>()

    private var lastWorkingState = false

    init {
        fun registerSyncFluidPart(key: String, tank: FluidTankEx) {
            this.registerSyncPart(key, IPayloadWriter { buffer ->
                val fluid = tank.fluid
                if ((fluid == null) || (fluid.amount == 0)) {
                    buffer.writeInt(0)
                } else {
                    buffer.writeInt(fluid.amount)
                    buffer.writeString(fluid.fluid.name)
                }
            }, IPayloadReceiver { _, buffer ->
                val fluidAmount = buffer.readInt()
                tank.fluid = if (fluidAmount > 0) {
                    val fluidName = buffer.readString()
                    FluidStack(FluidRegistry.getFluid(fluidName), fluidAmount)
                } else null
                null
            })
        }
        registerSyncFluidPart(STORAGE_FLUID_TANK, this.fluidTank)
        registerSyncFluidPart(STORAGE_RESIDUE_TANK, this.residueTank)

        registerSyncPart(STORAGE_ITEM_STACK, IPayloadWriter { buffer ->
            buffer.writeCompoundTag(this.itemHandler.serializeNBT())
        }, IPayloadReceiver { _, buffer ->
            this.itemHandler.deserializeNBT(buffer.readCompoundTag())
            null
        })
    }

    //#region fluid / inventory      methods

    protected open fun canInsertItem(item: ItemStack) = false
    protected open fun canExtractItem(item: ItemStack) = false
    protected open fun canFillFluidType(fluid: FluidStack) = false
    protected open fun canDrainFluidType(fluid: FluidStack) = false

    protected open fun getItemStackLimit(stack: ItemStack) = 1

    override fun getItemStack() = this.itemHandler.getStackInSlot(0)
    override var renderAngle: Float = 0f

    override final fun getFluidStacks() =
        arrayOf(this.fluidTank, this.residueTank)
            .mapNotNull { it.fluid?.copy() }
            .toTypedArray()

    override final fun getTotalCapacity() = this.fluidTank.capacity + this.residueTank.capacity

    //#endregion
    //#region capabilities & storage methods

    private val residueFluidCap = object: IFluidHandler {
        override fun drain(resource: FluidStack?, doDrain: Boolean) =
            this@BaseOreProcessorMachine.residueTank.drain(resource, doDrain)

        override fun drain(maxDrain: Int, doDrain: Boolean) =
            this@BaseOreProcessorMachine.residueTank.drain(maxDrain, doDrain)

        override fun fill(resource: FluidStack?, doFill: Boolean) = 0

        override fun getTankProperties() =
            this@BaseOreProcessorMachine.residueTank.tankProperties
    }

    private val mainFluidCap = object: IFluidHandler {
        override fun drain(resource: FluidStack?, doDrain: Boolean) =
            this@BaseOreProcessorMachine.fluidTank.drain(resource, doDrain)

        override fun drain(maxDrain: Int, doDrain: Boolean) =
            this@BaseOreProcessorMachine.fluidTank.drain(maxDrain, doDrain)

        override fun fill(resource: FluidStack?, doFill: Boolean) =
            this@BaseOreProcessorMachine.fluidTank.fill(resource, doFill)

        override fun getTankProperties() =
            this@BaseOreProcessorMachine.fluidTank.tankProperties
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) =
        (this.canReceiveEnergyOnSide(facing) && ((capability == MjAPI.CAP_RECEIVER) || (capability == MjAPI.CAP_CONNECTOR)))
            || ((facing != null) && EnumFacing.HORIZONTALS.contains(facing) && (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
            || ((facing == EnumFacing.UP) && (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY))
            || ((facing == EnumFacing.DOWN) && (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY))
            || super.hasCapability(capability, facing)

    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        if (this.canReceiveEnergyOnSide(facing) && (capability == MjAPI.CAP_CONNECTOR)) {
            return MjAPI.CAP_CONNECTOR.cast(this.batteryReceiver)
        }
        else if (this.canReceiveEnergyOnSide(facing) && (capability == MjAPI.CAP_RECEIVER)) {
            return MjAPI.CAP_RECEIVER.cast(this.batteryReceiver)
        }
        else if ((facing != null) && EnumFacing.HORIZONTALS.contains(facing) && (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemHandler)
        }
        else if ((facing == EnumFacing.UP) && (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.residueFluidCap)
        }
        else if ((facing == EnumFacing.DOWN) && (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.mainFluidCap)
        }
        return super.getCapability(capability, facing)
    }

    open fun canReceiveEnergyOnSide(side: EnumFacing?) =
        (side != null) && EnumFacing.HORIZONTALS.contains(side)

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)

        this.fluidTank.deserialize(compound, STORAGE_FLUID_TANK)
        this.residueTank.deserialize(compound, STORAGE_RESIDUE_TANK)
        this.itemHandler.deserialize(compound, STORAGE_ITEM_STACK)
        this.battery.deserialize(compound, STORAGE_BATTERY)
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound =
        super.writeToNBT(compound).also {
            this.fluidTank.serialize(compound, STORAGE_FLUID_TANK)
            this.residueTank.serialize(compound, STORAGE_RESIDUE_TANK)
            this.itemHandler.serialize(compound, STORAGE_ITEM_STACK)
            this.battery.serialize(compound, STORAGE_BATTERY)
        }

    //#endregion
    //#region sync                   methods

    protected fun registerSyncPart(key: String, writer: IPayloadWriter, receiver: IPayloadReceiver) {
        this.syncWriters[key] = writer
        this.syncReceivers[key] = receiver
    }

    override fun getUpdateTag() = this.writeToNBT(super.getUpdateTag())

    protected fun markForUpdate(key: String? = null) {
        this.markDirty()
        this.getWorld()?.also {
            if (!it.isRemote) {
                if (key.isNullOrBlank()/* || !this.syncWriters.containsKey(key)*/) {
                    this.syncEverything = true
                }
                else {
                    this.syncOnNextTick.add(key!!)
                }
            }
        }
    }

    private fun sendPayload() {
        val payload = PacketBufferBC(Unpooled.buffer())

        when {
            this.syncEverything -> {
                payload.writeBoolean(true)
                payload.writeCompoundTag(this.updateTag)
//                BCOreProcessing.logger.info("Sending full sync package.")
            }
            this.syncOnNextTick.isNotEmpty() -> {
                payload.writeBoolean(false)
                val keys = this.syncOnNextTick.filter {
                    this.syncWriters.containsKey(it)
                }
                if (keys.count() == 0) {
                    return
                }
//                BCOreProcessing.logger.info("Sending partial sync package [${keys.joinToString(", ")}].")
                payload.writeInt(keys.count())
                keys.forEach {
                    payload.writeString(it)
                    this.syncWriters[it]!!.write(payload)
                }
            }
            else -> return
        }

        this.syncEverything = false
        this.syncOnNextTick.clear()

        MessageUtil.sendToAllWatching(this.getWorld(), this.getPos(), MessageUpdateTile(this.getPos(), payload))
    }

    override fun receivePayload(ctx: MessageContext, buffer: PacketBufferBC): IMessage? {
        val flag = buffer.readBoolean()
        if (flag) {
//            BCOreProcessing.logger.info("Receiving full sync package.")
            buffer.readCompoundTag()?.also { this.readFromNBT(it) }
        }
        else {
            val keys = buffer.readInt()
//            BCOreProcessing.logger.info("Receiving partial sync package [$keys keys].")
            (0 until keys).forEach {
                val key = buffer.readString()
                if (this.syncReceivers.containsKey(key)) {
                    this.syncReceivers[key]!!.receivePayload(ctx, buffer)
                }
                else {
                    BCOreProcessing.logger.error("[${this.javaClass.name}] Received unknown sync part: '$key'. Stopping message.")
                    return null
                }
            }
        }
        return null
    }

    //#endregion
    //#region blockstate             methods

    protected open fun isWorking() =
        !this.itemHandler.getStackInSlot(0).isEmpty ||
            (this.fluidTank.fluidAmount > 0) ||
            (this.residueTank.fluidAmount > 0)

    private fun testWorkingState() {
        if (this.getWorld().isRemote) {
            val working = this.isWorking()
            if (working != lastWorkingState) {
                (this.getBlockType() as? BaseOreProcessorBlock<*>)
                    ?.setIsWorking(this.getWorld(), this.getPos(), working)
                this.lastWorkingState = working
            }
        }
    }

    //#endregion

    override final fun update() {
        this.innerUpdate()

        if (this.getWorld().isRemote) {
            this.renderAngle = (this.renderAngle + 360f / 50f) % 360f
            this.testWorkingState()
        }
        else {
            this.battery.tick(this.world, this.pos)
            this.sendPayload()
        }
    }

    protected open fun innerUpdate() { }

    companion object {
        private const val STORAGE_FLUID_TANK = "fluid_tank"
        private const val STORAGE_RESIDUE_TANK = "residue_Tank"
        private const val STORAGE_ITEM_STACK = "item_handler"
        private const val STORAGE_BATTERY = "mj_battery"
    }
}
