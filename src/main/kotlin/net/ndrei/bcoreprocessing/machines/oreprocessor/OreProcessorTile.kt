package net.ndrei.bcoreprocessing.machines.oreprocessor

import buildcraft.api.mj.MjAPI
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.IFluidTank
import net.minecraftforge.items.IItemHandler
import net.ndrei.bcoreprocessing.lib.recipes.OreProcessorRecipeManager
import net.ndrei.bcoreprocessing.lib.render.FluidStacksTESR
import net.ndrei.bcoreprocessing.lib.render.IFluidStacksHolder
import net.ndrei.bcoreprocessing.lib.render.IItemStackHolder
import net.ndrei.bcoreprocessing.lib.render.ItemStackTESR
import net.ndrei.bcoreprocessing.machines.BaseMJMachine
import net.ndrei.teslacorelib.gui.BasicTeslaGuiContainer
import net.ndrei.teslacorelib.gui.LockedInventoryTogglePiece
import net.ndrei.teslacorelib.inventory.BoundingRectangle
import net.ndrei.teslacorelib.inventory.FluidTankType

class OreProcessorTile
    : BaseMJMachine(OreProcessorTile::class.java.name.hashCode()), IItemStackHolder, IFluidStacksHolder {

    private lateinit var inputs: IItemHandler
    private lateinit var output1: IFluidTank
    private lateinit var output2: IFluidTank

    private var currentInput: ItemStack = ItemStack.EMPTY
    private var currentTick: Int = 0

    //#region gui, storage & inventory

    override fun getRenderers() = super.getRenderers().also {
        it.add(ItemStackTESR)
        it.add(FluidStacksTESR)
    }

    override fun canReceiveEnergyOnSide(side: EnumFacing?) =
        (side != null) && EnumFacing.HORIZONTALS.contains(side)

    override fun getItemStack() = this.currentInput.copy()

    override fun getFluidStacks() =
        arrayOf(this.output1, this.output2)
            .mapNotNull { it.fluid?.copy() }
            .toTypedArray()

    override fun getTotalCapacity() = 15000

    override fun initializeInventories() {
        super.initializeInventories()

        this.inputs = super.addSimpleInventory(3, "inputs", EnumDyeColor.GREEN, "Input Inventory",
            BoundingRectangle(43, 25, 18, 54),
            { stack, _ -> OreProcessorRecipeManager.findFirstRecipe(stack, true) != null },
            { _, _ -> false },
            true)
        super.sideConfig.setSidesForColor(EnumDyeColor.GREEN, EnumFacing.HORIZONTALS.toList())

        this.output1 = this.addSimpleFluidTank(9000, "Molten Ore Tank", EnumDyeColor.BROWN,
            97, 25, FluidTankType.OUTPUT)
        super.sideConfig.setSidesForColor(EnumDyeColor.BROWN, listOf(EnumFacing.DOWN))

        this.output2 = this.addSimpleFluidTank(6000, "Residue Tank", EnumDyeColor.RED,
            133, 25, FluidTankType.OUTPUT)
        super.sideConfig.setSidesForColor(EnumDyeColor.RED, listOf(EnumFacing.UP))
    }

    override fun getGuiContainerPieces(container: BasicTeslaGuiContainer<*>) =
        super.getGuiContainerPieces(container).also {
            it.add(LockedInventoryTogglePiece(28, 45, this, EnumDyeColor.GREEN))
        }

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)

        if (compound.hasKey("current_stack", Constants.NBT.TAG_COMPOUND)) {
            this.currentInput = ItemStack(compound.getCompoundTag("current_stack"))
            this.currentTick = compound.getInteger("current_tick")
        }
    }

    override fun writeToNBT(compound: NBTTagCompound) =
        super.writeToNBT(compound).also {
            if (!this.currentInput.isEmpty) {
                it.setTag("current_stack", this.currentInput.serializeNBT())
                it.setInteger("current_tick", this.currentTick)
            }
        }

    //#endregion

    override fun innerUpdate() {
        if (this.currentInput.isEmpty) {
            // try to find a valid input
            val recipe = (0 until this.inputs.slots).mapNotNull {
                val stack = this.inputs.getStackInSlot(it)
                return@mapNotNull if (stack.isEmpty) {
                    null
                }
                else {
                    OreProcessorRecipeManager.findFirstRecipe(stack, false).let { r ->
                        if (r == null) null else Pair(stack, r)
                    }
                }
            }.firstOrNull()
            if (recipe != null) {
                this.currentInput = recipe.second.processInput(recipe.first)
                this.currentTick = 0

                this.markDirty()
                this.forceSync()
            }
        }

        val power = 6 * MjAPI.ONE_MINECRAFT_JOULE
        if (!this.currentInput.isEmpty && (this.battery.stored >= power)) {
            var processed = false

            val recipe = OreProcessorRecipeManager.findFirstRecipe(this.currentInput, false) ?: return
            val fluids = recipe.getOutputForTick(this.currentTick)
            if (!fluids.isEmpty()) {
                val fluid1 = fluids[0]
                val f1ok = (fluid1.amount == 0)|| (this.output1.fill(fluid1, false) == fluid1.amount)

                val fluid2 = if (fluids.size > 1) fluids[1] else null
                val f2ok = (fluid2 == null) || (fluid2.amount == 0) || (this.output2.fill(fluid2, false) == fluid2.amount)

                if (f1ok && f2ok) {
                    if (fluid1.amount > 0) {
                        this.output1.fill(fluid1, true)
                    }

                    if ((fluid2 != null) && (fluid2.amount > 0)) {
                        this.output2.fill(fluid2, true)
                    }

                    processed = true
                }
            }
            if (processed) {
                this.currentTick++
                if (this.currentTick >= recipe.getProcessingTicks()) {
                    this.currentInput = ItemStack.EMPTY
                    this.currentTick = 0
                }

                this.battery.extractPower(power)
            }
        }
    }
}
