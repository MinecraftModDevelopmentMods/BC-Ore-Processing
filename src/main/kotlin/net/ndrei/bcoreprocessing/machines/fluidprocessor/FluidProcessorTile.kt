package net.ndrei.bcoreprocessing.machines.fluidprocessor

import buildcraft.api.mj.MjAPI
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.lib.copyWithSize
import net.ndrei.bcoreprocessing.lib.recipes.FluidProcessorRecipeManager
import net.ndrei.bcoreprocessing.machines.BaseOreProcessorMachine

class FluidProcessorTile
    : BaseOreProcessorMachine() {

    private var currentFluid: FluidStack? = null
    private var currentTick: Int = 0

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
        this.currentFluid = if (compound.hasKey("current_fluid", Constants.NBT.TAG_COMPOUND)) {
            FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("current_fluid"))
        } else null
    }

    override fun writeToNBT(compound: NBTTagCompound) =
        super.writeToNBT(compound).also {
            it.setInteger("current_tick", this.currentTick)
            if (this.currentFluid != null) {
               it.setTag("current_fluid", this.currentFluid!!.writeToNBT(NBTTagCompound()))
            }
        }

    //#endregion

    override fun update() {
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
                        }
                    }

                    this.currentTick++
                    if (this.currentTick >= recipe.getProcessingTicks()) {
                        this.currentTick = 0
                        this.currentFluid = null
                        this.itemHandler.setStackInSlot(0, recipe.getRecipeOutput().first)
                    }

                    this.battery.extractPower(power)
                }
            }
        }

        super.update()
    }
}
