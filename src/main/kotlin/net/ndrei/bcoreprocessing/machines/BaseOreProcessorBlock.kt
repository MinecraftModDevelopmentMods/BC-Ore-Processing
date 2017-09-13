package net.ndrei.bcoreprocessing.machines

import net.minecraft.block.Block
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.registries.IForgeRegistry
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

        this.defaultState = this.blockState.baseState
            .withProperty(FACING, EnumFacing.NORTH)
            .withProperty(WORKING, false)
    }

    fun registerBlock(registry: IForgeRegistry<Block>) {
        registry.register(this)
        GameRegistry.registerTileEntity(this.teClass, this.registryName!!.toString() + "_tile")
    }

    fun registerItem(registry: IForgeRegistry<Item>) {
        val item = ItemBlock(this)
        item.registryName = this.registryName
        registry.register(item)
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

    fun setIsWorking(worldIn: World, pos: BlockPos, working: Boolean) {
        val tileEntity = worldIn.getTileEntity(pos)
        worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(WORKING, working))
        if (tileEntity != null) {
            tileEntity.validate()
            worldIn.setTileEntity(pos, tileEntity)
            worldIn.notifyNeighborsOfStateChange(pos, this, true)
        }
    }

    override fun getBlockLayer() = BlockRenderLayer.TRANSLUCENT
    override fun isOpaqueCube(state: IBlockState?) = false
    override fun isFullCube(state: IBlockState) = false

    override fun getStateForPlacement(worldIn: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase): IBlockState {
        return this.defaultState.withProperty(FACING, placer.horizontalFacing.opposite)
    }

    override fun createBlockState(): BlockStateContainer {
        return BlockStateContainer(this, FACING, WORKING)
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        val state = (meta and 1) != 0
        var enumfacing = EnumFacing.getFront(meta shr 1)
        if (enumfacing.axis == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH
        }
        return this.defaultState
            .withProperty(FACING, enumfacing)
            .withProperty(WORKING, state)
    }

    override fun getMetaFromState(state: IBlockState): Int {
        var meta = state.getValue(FACING).index
        meta = meta shl 1
        meta += if (state.getValue(WORKING)) 1 else 0
        return meta
    }

    companion object {
        private val FACING = BlockHorizontal.FACING!!
        private val WORKING = PropertyBool.create("working")
    }
}