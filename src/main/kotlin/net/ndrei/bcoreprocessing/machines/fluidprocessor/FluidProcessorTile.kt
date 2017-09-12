package net.ndrei.bcoreprocessing.machines.fluidprocessor

import buildcraft.api.mj.MjAPI
import buildcraft.lib.net.IPayloadReceiver
import buildcraft.lib.net.IPayloadWriter
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.lib.copyWithSize
import net.ndrei.bcoreprocessing.lib.recipes.FluidProcessorRecipeManager
import net.ndrei.bcoreprocessing.machines.BaseOreProcessorMachine

class FluidProcessorTile
    : BaseOreProcessorMachine() {

    private var currentFluid: FluidStack? = null
    private var currentTick: Int = 0

    init {
        super.registerSyncPart(STORAGE_CURRENT_FLUID, IPayloadWriter { buffer ->
            val fluid = this.currentFluid
            if ((fluid == null) || (fluid.amount == 0)) {
                buffer.writeInt(0)
            } else {
                buffer.writeInt(fluid.amount)
                buffer.writeString(fluid.fluid.name)
            }
        }, IPayloadReceiver { _, buffer ->
            val fluidAmount = buffer.readInt()
            this.currentFluid = if (fluidAmount > 0) {
                val fluidName = buffer.readString()
                FluidStack(FluidRegistry.getFluid(fluidName), fluidAmount)
            } else null
            null
        })
    }

    //#region storage & inventory

    override fun canExtractItem(item: ItemStack) = true

    override fun canFillFluidType(fluid: FluidStack) =
        null != FluidProcessorRecipeManager.findFirstRecipe(fluid, true)

    override fun getItemStack() =
        (this.currentFluid ?: this.fluidTank.fluid)?.let {
            FluidProcessorRecipeManager.findFirstRecipe(it, true)?.getRecipeOutput()?.first
        } ?: this.itemHandler.getStackInSlot(0)

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)

        this.currentTick = if (compound.hasKey("current_tick", Constants.NBT.TAG_COMPOUND)) {
            compound.getInteger("current_tick")
        } else 0
        this.currentFluid = if (compound.hasKey(STORAGE_CURRENT_FLUID, Constants.NBT.TAG_COMPOUND)) {
            FluidStack.loadFluidStackFromNBT(compound.getCompoundTag(STORAGE_CURRENT_FLUID))
        } else null
    }

    override fun writeToNBT(compound: NBTTagCompound) =
        super.writeToNBT(compound).also {
            it.setInteger("current_tick", this.currentTick)
            if (this.currentFluid != null) {
               it.setTag(STORAGE_CURRENT_FLUID, this.currentFluid!!.writeToNBT(NBTTagCompound()))
            }
        }

    //#endregion

    override fun innerUpdate() {
        val power = 6 * MjAPI.ONE_MINECRAFT_JOULE
        val inputFluid = this.currentFluid ?: this.fluidTank.fluid
        if ((inputFluid != null) && (inputFluid.amount > 0) && (this.battery.stored >= power) && this.itemHandler.getStackInSlot(0).isEmpty) {
            val recipe = FluidProcessorRecipeManager.findFirstRecipe(inputFluid, true)
            if (recipe != null) {
                val outputs = recipe.getOutputForTick(this.currentTick)
                val fluidAtTick = recipe.getInputForTick(this.currentTick)

                val fluid = outputs.second
                val f2ok = (fluid == null) || (fluid.amount == 0) || (this.residueTank.fillInternal(fluid, false) == fluid.amount)

                val drainOk = (fluidAtTick == null) || (fluidAtTick.amount == 0) || (this.fluidTank.drainInternal(fluidAtTick, false)?.amount == fluidAtTick.amount)

                if (f2ok && drainOk) {
                    if ((fluid != null) && (fluid.amount > 0)) {
                        this.residueTank.fillInternal(fluid, true)
                    }

                    if ((fluidAtTick != null) && (fluidAtTick.amount > 0)) {
                        this.fluidTank.drainInternal(fluidAtTick, true)
                        if (this.currentFluid == null) {
                            this.currentFluid = fluidAtTick.copyWithSize(1)
                            this.markForUpdate(STORAGE_CURRENT_FLUID)
                        }
                    }

                    this.currentTick++
                    if (this.currentTick >= recipe.getProcessingTicks()) {
                        this.currentTick = 0
                        this.currentFluid = null
                        this.markForUpdate(STORAGE_CURRENT_FLUID)
                        this.itemHandler.setStackInSlot(0, recipe.getRecipeOutput().first)
                    }

                    this.battery.extractPower(power)
                }
            }
        }
    }

    companion object {
        protected const val STORAGE_CURRENT_FLUID = "current_fluid"
    }
}
