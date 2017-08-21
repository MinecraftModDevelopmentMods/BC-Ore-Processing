package net.ndrei.bcoreprocessing

import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.ndrei.teslacorelib.BaseProxy

@Suppress("unused")
open class CommonProxy(side: Side) : BaseProxy(side) {
    constructor() : this(Side.SERVER)
}

@SideOnly(Side.CLIENT)
@Suppress("unused")
class ClientProxy : CommonProxy(Side.CLIENT)

@SideOnly(Side.SERVER)
@Suppress("unused")
class ServerProxy : CommonProxy(Side.SERVER)
