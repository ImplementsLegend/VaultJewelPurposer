package implementslegendkt.mod.screenlegends

import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.AbstractContainerMenu

class PlainScreen<M : AbstractContainerMenu> (
    menu: M,
    p_96550_: Component?,
    @JvmField val compositions: List<Composition<in PlainScreen<M>>>
) : DecentScreen<PlainScreen<M>, M>(menu, p_96550_)
