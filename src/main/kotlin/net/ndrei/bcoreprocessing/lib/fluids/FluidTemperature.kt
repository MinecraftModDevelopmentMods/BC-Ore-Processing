package net.ndrei.bcoreprocessing.lib.fluids

enum class FluidTemperature(val baseIngots: Int, val baseResidue: Int) {
    COOL(3, 10),
    HOT(2, 25),
    SEARING(3, 50)
}
