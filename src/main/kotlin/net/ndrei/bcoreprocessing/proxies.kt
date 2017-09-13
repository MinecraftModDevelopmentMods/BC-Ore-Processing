package net.ndrei.bcoreprocessing

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.ndrei.bcoreprocessing.lib.fluids.FluidsRegistry
import net.ndrei.bcoreprocessing.lib.recipes.FluidProcessorRecipeManager
import net.ndrei.bcoreprocessing.lib.recipes.OreProcessorRecipeManager
import net.ndrei.bcoreprocessing.machines.fluidprocessor.FluidProcessorBlock
import net.ndrei.bcoreprocessing.machines.oreprocessor.OreProcessorBlock

@Suppress("unused")
open class CommonProxy(val side: Side) {
    open fun preInit(ev: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)

        // TODO: find a way to move this to the corresponding registries
        this.getModBlocks().forEach { it.registerBlock() }
        this.getModBlocks().forEach { it.registerItem() }

        // "crucial" for this to happen before XXXProcessorRecipeManager.registerRecipes()
        FluidsRegistry.registerFluids()
    }

    open fun init(ev: FMLInitializationEvent) {
        this.getModBlocks().forEach { it.registerRecipe() }

        OreProcessorRecipeManager.registerRecipes()
        FluidProcessorRecipeManager.registerRecipes()
    }

    open fun postInit(ev: FMLPostInitializationEvent) {
    }

    protected fun getModBlocks() =
        arrayOf(OreProcessorBlock, FluidProcessorBlock)

//    @SubscribeEvent
//    fun registerBlocks(ev: RegistryEvent.Register<Block>) {
//        this.getModBlocks().forEach { it.registerBlock(ev.registry) }
//        FluidsRegistry.registerFluids()
//    }

//    @SubscribeEvent
//    fun registerItems(ev: RegistryEvent.Register<Item>) {
//        this.getModBlocks().forEach { it.registerItem(ev.registry) }
//    }
}

@SideOnly(Side.CLIENT)
@Suppress("unused")
class ClientProxy : CommonProxy(Side.CLIENT) {
    override fun preInit(ev: FMLPreInitializationEvent) {
        super.preInit(ev)
        super.getModBlocks().forEach { it.registerRenderer() }
    }
}

@SideOnly(Side.SERVER)
@Suppress("unused")
class ServerProxy : CommonProxy(Side.SERVER)
