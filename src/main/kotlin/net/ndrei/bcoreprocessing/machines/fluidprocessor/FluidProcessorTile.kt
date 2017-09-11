package net.ndrei.bcoreprocessing.machines.fluidprocessor

import buildcraft.api.mj.MjAPI
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.lib.recipes.FluidProcessorRecipeManager
import net.ndrei.bcoreprocessing.machines.BaseOreProcessorMachine

class FluidProcessorTile
    : BaseOreProcessorMachine() {

    private var currentTick: Int = 0

    //#region storage & inventory

    override fun canExtractItem(item: ItemStack) = true

    override fun canFillFluidType(fluid: FluidStack) =
        null != FluidProcessorRecipeManager.findFirstRecipe(fluid, true)

    override fun getItemStack() =
        this.fluidTank.fluid?.let {
            FluidProcessorRecipeManager.findFirstRecipe(it, true)?.getRecipeOutput()?.first
        } ?: this.itemHandler.getStackInSlot(0)

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)

        if (compound.hasKey("current_tick", Constants.NBT.TAG_COMPOUND)) {
            this.currentTick = compound.getInteger("current_tick")
        }
    }

    override fun writeToNBT(compound: NBTTagCompound) =
        super.writeToNBT(compound).also {
            it.setInteger("current_tick", this.currentTick)
        }

    //#endregion

    override fun update() {
        val power = 6 * MjAPI.ONE_MINECRAFT_JOULE
        val inputFluid = this.fluidTank.fluid
        if ((inputFluid != null) && (inputFluid.amount > 0) && (this.battery.stored >= power) && this.itemHandler.getStackInSlot(0).isEmpty) {
            val recipe = FluidProcessorRecipeManager.findFirstRecipe(inputFluid, true) ?: return
            val outputs = recipe.getOutputForTick(this.currentTick)
            val fluidAtTick = recipe.getInputForTick(this.currentTick)

            val fluid = outputs.second
            val f2ok = (fluid == null) || (fluid.amount == 0) || (this.residueTank.fillInternal(fluid, false) == fluid.amount)

            val drainOk = (fluidAtTick == null) || (fluidAtTick.amount == 0) || (this.fluidTank.drainInternal(fluidAtTick.amount, false)?.amount == fluidAtTick.amount)

            if (!f2ok || !drainOk) {
                // something could not be processed, skip tick
                return
            }

            if ((fluid != null) && (fluid.amount > 0)) {
                this.residueTank.fillInternal(fluid, true)
            }

            if ((fluidAtTick != null) && (fluidAtTick.amount > 0)) {
                this.fluidTank.drainInternal(fluidAtTick, true)
            }

            this.currentTick++
            if (this.currentTick >= recipe.getProcessingTicks()) {
                this.currentTick = 0
                this.itemHandler.setStackInSlot(0, recipe.getRecipeOutput().first)
            }

            this.battery.extractPower(power)
        }

        super.update()
    }
}
