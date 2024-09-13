package implementslegendkt.mod.vaultjp

import net.minecraft.core.Direction
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack

interface DefaultWorldlyContainer : WorldlyContainer {
    override fun getSlotsForFace(p_19238_: Direction): IntArray = IntArray(containerSize) { it }

    override fun canPlaceItemThroughFace(p_19235_: Int, p_19236_: ItemStack, p_19237_: Direction?): Boolean = true

    override fun canTakeItemThroughFace(p_19239_: Int, p_19240_: ItemStack, p_19241_: Direction): Boolean = true
}
