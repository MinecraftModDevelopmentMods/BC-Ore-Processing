package net.ndrei.bcoreprocessing.machines.fluidprocessor

import buildcraft.api.mj.MjAPI
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.IFluidTank
import net.minecraftforge.items.IItemHandler
import net.ndrei.bcoreprocessing.lib.recipes.FluidProcessorRecipeManager
import net.ndrei.bcoreprocessing.lib.render.FluidStacksTESR
import net.ndrei.bcoreprocessing.lib.render.IFluidStacksHolder
import net.ndrei.bcoreprocessing.lib.render.IItemStackHolder
import net.ndrei.bcoreprocessing.lib.render.ItemStackTESR
import net.ndrei.bcoreprocessing.machines.BaseMJMachine
import net.ndrei.teslacorelib.inventory.BoundingRectangle
import net.ndrei.teslacorelib.inventory.FluidTankType
import net.ndrei.teslacorelib.utils.insertItems

class FluidProcessorTile
    : BaseMJMachine(FluidProcessorTile::class.java.name.hashCode()), IItemStackHolder, IFluidStacksHolder {

    private lateinit var inputFluid: IFluidTank
    private lateinit var residueTank: IFluidTank
    private lateinit var outputs: IItemHandler

    private var currentInput: FluidStack? = null
    private var currentTick: Int = 0

    //#region gui, storage & inventory

    override fun getRenderers() = super.getRenderers().also {
        it.add(ItemStackTESR)
        it.add(FluidStacksTESR)
    }

    override fun canReceiveEnergyOnSide(side: EnumFacing?) =
            (side != null) && EnumFacing.HORIZONTALS.contains(side)

    override fun getItemStack() = ItemStack.EMPTY!!

    override fun getFluidStacks() =
            arrayOf(this.inputFluid, this.residueTank)
                    .mapNotNull { it.fluid?.copy() }
                    .toTypedArray()

    override fun getTotalCapacity() = 15000

    override fun initializeInventories() {
        super.initializeInventories()

        this.inputFluid = this.addSimpleFluidTank(9000, "Input Fluid", EnumDyeColor.BLUE,
                43, 25, FluidTankType.INPUT, {
            FluidProcessorRecipeManager.findFirstRecipe(it, true) != null
        })
        super.sideConfig.setSidesForColor(EnumDyeColor.BLUE, EnumFacing.HORIZONTALS.toList())

        this.outputs = super.addSimpleInventory(3, "outputs", EnumDyeColor.PURPLE, "Output Inventory",
                BoundingRectangle(97, 25, 18, 54),
                { _, _ -> false }, { _, _ -> true }, false)
        super.sideConfig.setSidesForColor(EnumDyeColor.PURPLE, listOf(EnumFacing.DOWN))

        this.residueTank = this.addSimpleFluidTank(6000, "Residue Tank", EnumDyeColor.RED,
                133, 25, FluidTankType.OUTPUT)
        super.sideConfig.setSidesForColor(EnumDyeColor.RED, listOf(EnumFacing.UP))
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)

        if (compound.hasKey("current_fluid", Constants.NBT.TAG_COMPOUND)) {
            this.currentInput = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("current_fluid"))
            this.currentTick = compound.getInteger("current_tick")
        }
    }

    override fun writeToNBT(compound: NBTTagCompound) =
            super.writeToNBT(compound).also {
                if (this.currentInput != null) {
                    it.setTag("current_fluid", this.currentInput!!.writeToNBT(NBTTagCompound()))
                    it.setInteger("current_tick", this.currentTick)
                }
            }

    //#endregion

    override fun innerUpdate() {
        if (this.currentInput == null) {
            // try to find a valid input
            val fluid = this.inputFluid.fluid
            if ((fluid != null) && (fluid.amount > 0)) {
                val recipe = FluidProcessorRecipeManager.findFirstRecipe(fluid, false)
                if (recipe != null) {
                    this.currentInput = recipe.processInput(fluid)
                    if (fluid.amount == 0) {
                        // hack around tanks getting stuck on fluid types when amount == 0
                        fluid.amount = 1
                        this.inputFluid.drain(1, true)
                    }
                    this.currentTick = 0

                    this.markDirty()
                    this.forceSync()
                }
            }
        }

        val power = 6 * MjAPI.ONE_MINECRAFT_JOULE
        if ((this.currentInput != null) && (this.battery.stored >= power)) {
            val recipe = FluidProcessorRecipeManager.findFirstRecipe(this.currentInput!!, false) ?: return
            val outputs = recipe.getOutputForTick(this.currentTick)
            if (!outputs.first.isEmpty || (outputs.second != null)) {
                val item = outputs.first
                val f1ok = (item.isEmpty) || this.outputs.insertItems(item, true).isEmpty

                val fluid = outputs.second
                val f2ok = (fluid == null) || (fluid.amount == 0) || (this.residueTank.fill(fluid, false) == fluid.amount)

                if (!f1ok || !f2ok) {
                    // something could not be processed, skip tick
                    return
                }

                if (!item.isEmpty) {
                    this.outputs.insertItems(item, false)
                }

                if ((fluid != null) && (fluid.amount > 0)) {
                    this.residueTank.fill(fluid, true)
                }
            }

            this.currentTick++
            if (this.currentTick >= recipe.getProcessingTicks()) {
                this.currentInput = null
                this.currentTick = 0
            }

            this.battery.extractPower(power)
        }
    }
}
