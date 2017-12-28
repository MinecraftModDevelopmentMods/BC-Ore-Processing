package net.ndrei.bcoreprocessing.machines.oreprocessor

import net.ndrei.bcoreprocessing.machines.BaseOreProcessorBlock

object OreProcessorBlock
    : BaseOreProcessorBlock<OreProcessorTile>("ore_processor", OreProcessorTile::class.java)