package net.ndrei.bcoreprocessing

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLConstructionEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.ndrei.bcoreprocessing.api.recipes.OreProcessingRecipes
import net.ndrei.bcoreprocessing.lib.config.ModConfigHandler
import net.ndrei.bcoreprocessing.lib.recipes.FluidProcessorRecipeManager
import net.ndrei.bcoreprocessing.lib.recipes.OreProcessorRecipeManager
import net.ndrei.bcoreprocessing.machines.oreprocessor.OreProcessorBlock
import org.apache.logging.log4j.Logger

@Suppress("unused")
@Mod(modid = MOD_ID, version = MOD_VERSION, name = MOD_NAME,
    acceptedMinecraftVersions = MOD_MC_VERSION,
    dependencies = MOD_DEPENDENCIES,
    useMetadata = true, modLanguage = "kotlin", modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter")
object BCOreProcessing {
    @SidedProxy(clientSide = "net.ndrei.bcoreprocessing.ClientProxy", serverSide = "net.ndrei.bcoreprocessing.ServerProxy")
    lateinit var proxy: CommonProxy
    lateinit var logger: Logger
    lateinit var configHelper: ModConfigHandler

    val creativeTab: CreativeTabs = object : CreativeTabs("BC Ore Processing") {
        override fun getIconItemStack() = ItemStack(OreProcessorBlock)
        override fun getTabIconItem() = this.iconItemStack
    }

    @Mod.EventHandler
    fun construction(event: FMLConstructionEvent) {
        // Use forge universal bucket
        FluidRegistry.enableUniversalBucket()

        OreProcessingRecipes.init(OreProcessorRecipeManager, FluidProcessorRecipeManager)
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        BCOreProcessing.logger = event.modLog
        BCOreProcessing.configHelper = ModConfigHandler(MOD_ID, this.javaClass, this.logger, event.modConfigurationDirectory)

        BCOreProcessing.proxy.preInit(event)
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        BCOreProcessing.proxy.init(e)
    }

    @Mod.EventHandler
    fun postInit(e: FMLPostInitializationEvent) {
        BCOreProcessing.proxy.postInit(e)
    }
}
