package net.ndrei.bcoreprocessing.machines

import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.ndrei.bcoreprocessing.BCOreProcessing
import net.ndrei.bcoreprocessing.MOD_ID
import net.ndrei.bcoreprocessing.lib.render.FluidAndItemsTESR

abstract class BaseOreProcessorBlock<T : BaseOreProcessorMachine>(registryName: String, private val teClass: Class<T>)
    : Block(Material.ROCK), ITileEntityProvider {

    init {
        this.setRegistryName(MOD_ID, registryName)
        this.unlocalizedName = MOD_ID + "." + registryName
        this.setCreativeTab(BCOreProcessing.creativeTab)

        this.setHarvestLevel("pickaxe", 0)
        this.setHardness(3.0f)
    }

    fun registerBlock(/*registry: IForgeRegistry<Block>*/) {
        GameRegistry.register(this)
        GameRegistry.registerTileEntity(this.teClass, this.registryName!!.toString() + "_tile")
    }

    fun registerItem(/*registry: IForgeRegistry<Item>*/) {
        val item = ItemBlock(this)
        item.registryName = this.registryName
        GameRegistry.register(item)
    }

    @SideOnly(Side.CLIENT)
    fun registerRenderer() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0,
            ModelResourceLocation(this.registryName!!, "inventory")
        )

        ClientRegistry.bindTileEntitySpecialRenderer(this.teClass, FluidAndItemsTESR)
    }

    override fun createNewTileEntity(worldIn: World?, meta: Int): TileEntity? {
        return try {
            this.teClass.newInstance()
        } catch (e: Throwable) {
            BCOreProcessing.logger.error(e)
            null
        }
    }

    override fun getBlockLayer() = BlockRenderLayer.TRANSLUCENT
    override fun isOpaqueCube(state: IBlockState?) = false
}