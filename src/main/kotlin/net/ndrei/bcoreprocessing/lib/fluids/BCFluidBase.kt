package net.ndrei.bcoreprocessing.lib.fluids

import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fluids.Fluid

class BCFluidBase(val baseName: String, still: ResourceLocation, flowing: ResourceLocation, private val fluidColor: Int, val fluidTemperature: FluidTemperature, maxLuminosity: Int, density: Int, viscosity: Int, isGaseous: Boolean = false)
    : Fluid("bcop-$baseName-${fluidTemperature.name.toLowerCase()}", still, flowing) {
    init {
        this.setLuminosity(when (this.fluidTemperature) {
            FluidTemperature.COOL -> 0
            FluidTemperature.HOT -> maxLuminosity / 2
            FluidTemperature.SEARING -> maxLuminosity
        })
        this.setDensity(density)
        this.setViscosity(viscosity)
        this.setTemperature(when (this.fluidTemperature) {
            FluidTemperature.COOL -> 300
            FluidTemperature.HOT -> 800
            FluidTemperature.SEARING -> 1300
        })

        this.isGaseous = isGaseous
    }

    override fun getUnlocalizedName(): String {
        return "${if (this.isGaseous) "" else "Molten "}${this.baseName.split("_").map { it.capitalize() }.joinToString(" ")} (${when (this.fluidTemperature) {
            FluidTemperature.COOL -> TextFormatting.AQUA
            FluidTemperature.HOT -> TextFormatting.GOLD
            FluidTemperature.SEARING -> TextFormatting.RED
        }}${this.fluidTemperature.name.toLowerCase().capitalize()}${TextFormatting.RESET})"
        // "fluid.${MOD_ID}.${this.unlocalizedName}"
    }

    override fun getColor() =
        this.fluidColor.let {
            if ((it ushr 24) == 0) {
                (0xFF shl 24) or it
            }
            else it
        }
}
