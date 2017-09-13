package net.ndrei.bcoreprocessing

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
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
    }

    open fun init(ev: FMLInitializationEvent) {
    }

    open fun postInit(ev: FMLPostInitializationEvent) {
    }

    protected fun getModBlocks() =
        arrayOf(OreProcessorBlock, FluidProcessorBlock)

    @SubscribeEvent
    fun registerBlocks(ev: RegistryEvent.Register<Block>) {
        this.getModBlocks().forEach { it.registerBlock(ev.registry) }

        // "crucial" for this to happen before XXXProcessorRecipeManager.registerRecipes()
        FluidsRegistry.registerFluids()
    }

    @SubscribeEvent
    fun registerItems(ev: RegistryEvent.Register<Item>) {
        this.getModBlocks().forEach { it.registerItem(ev.registry) }
    }

    @SubscribeEvent
    fun registerRecipes(ev: RegistryEvent.Register<IRecipe>) {
        OreProcessorRecipeManager.registerRecipes()
        FluidProcessorRecipeManager.registerRecipes()
    }
}

@SideOnly(Side.CLIENT)
@Suppress("unused")
class ClientProxy : CommonProxy(Side.CLIENT) {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    fun registerModel(ev: ModelRegistryEvent) {
        super.getModBlocks().forEach { it.registerRenderer() }
    }
}

@SideOnly(Side.SERVER)
@Suppress("unused")
class ServerProxy : CommonProxy(Side.SERVER)
