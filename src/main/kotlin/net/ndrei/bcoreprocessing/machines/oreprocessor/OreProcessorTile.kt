package net.ndrei.bcoreprocessing.machines.oreprocessor

import buildcraft.api.mj.MjAPI
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.FluidStack
import net.ndrei.bcoreprocessing.lib.recipes.OreProcessorRecipeManager
import net.ndrei.bcoreprocessing.lib.render.IFluidStacksHolder
import net.ndrei.bcoreprocessing.lib.render.IItemStackHolder
import net.ndrei.bcoreprocessing.machines.BaseOreProcessorMachine

class OreProcessorTile
    : BaseOreProcessorMachine(), IItemStackHolder, IFluidStacksHolder {

    private var currentTick: Int = 0

    //#region storage & inventory

    override fun canInsertItem(item: ItemStack) =
        null != OreProcessorRecipeManager.findFirstRecipe(item, true)

    override fun canDrainFluidType(fluid: FluidStack) = true

    override fun getItemStackLimit(stack: ItemStack) =
        OreProcessorRecipeManager.findFirstRecipe(stack, true)?.getPossibleInputs()?.firstOrNull()?.count ?: 0

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)

        if (compound.hasKey("current_tick", Constants.NBT.TAG_COMPOUND)) {
            this.currentTick = compound.getInteger("current_tick")
        } else {
            this.currentTick = 0
        }
    }

    override fun writeToNBT(compound: NBTTagCompound) =
        super.writeToNBT(compound).also {
            it.setInteger("current_tick", this.currentTick)
        }

    //#endregion

    override fun innerUpdate() {
        val power = 6 * MjAPI.ONE_MINECRAFT_JOULE
        val currentInput = this.itemHandler.getStackInSlot(0)
        if (!currentInput.isEmpty && (this.battery.stored >= power)) {
            val recipe = OreProcessorRecipeManager.findFirstRecipe(currentInput, false)
            if (recipe != null) {
                val fluids = recipe.getOutputForTick(this.currentTick)
                if ((fluids.first != null) || (fluids.second != null)) {
                    val fluid1 = fluids.first
                    val f1ok = (fluid1 == null) || (fluid1.amount == 0) || (this.fluidTank.fillInternal(fluid1, false) == fluid1.amount)

                    val fluid2 = fluids.second
                    val f2ok = (fluid2 == null) || (fluid2.amount == 0) || (this.residueTank.fillInternal(fluid2, false) == fluid2.amount)

                    if (f1ok && f2ok) {
                        if ((fluid1 != null) && (fluid1.amount > 0)) {
                            this.fluidTank.fillInternal(fluid1, true)
                        }

                        if ((fluid2 != null) && (fluid2.amount > 0)) {
                            this.residueTank.fillInternal(fluid2, true)
                        }
                    }

                    this.currentTick++
                    if (this.currentTick >= recipe.getProcessingTicks()) {
                        this.currentTick = 0
                        this.itemHandler.setStackInSlot(0, ItemStack.EMPTY)
                    }

                    this.battery.extractPower(power)
                }
            }
        }
    }
}
