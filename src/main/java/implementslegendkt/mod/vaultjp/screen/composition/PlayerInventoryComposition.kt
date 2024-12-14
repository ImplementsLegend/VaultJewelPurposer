package implementslegendkt.mod.vaultjp.screen.composition

import implementslegendkt.mod.screenlegends.Composition
import implementslegendkt.mod.screenlegends.DecentScreen
import implementslegendkt.mod.screenlegends.view.BackgroundViewDSL
import implementslegendkt.mod.screenlegends.view.SlotViewDSL
import net.minecraft.client.renderer.Rect2i
import net.minecraft.resources.ResourceLocation
import oshi.util.tuples.Pair

@JvmRecord
data class PlayerInventoryComposition<S : DecentScreen<*, *>>(
    val hotbarStart: Int,
    val mainInvStart: Int,
    val x: Int,
    val y: Int
) : Composition<S> {
    override fun S.compose(midX: Int, midY: Int) {
        background {
            texture = { ResourceLocation("vaultjp:textures/gui/player_base_inv.png") }
            srcRect = { Rect2i(0, 0, 176, 90) }
            atlasSize = { 176 to 90 }
            pos = { midX + x to midY + y }
        }


        repeat(27) { slotIndex ->
            val x = slotIndex % 9
            val y = slotIndex / 9
            val slotCopy = slotIndex
            viewSlot {
                this.slot = { slotCopy + mainInvStart }
                position = { x * 18 + 8 + this@PlayerInventoryComposition.x + midX to y * 18 + 8 + midY + this@PlayerInventoryComposition.y }
            }
        }
        repeat(9) { slotIndex ->
            val x = slotIndex
            val slotCopy = slotIndex
            viewSlot {
                this.slot = { slotCopy + hotbarStart }
                position = { x * 18 + 8 + this@PlayerInventoryComposition.x + midX to 66 + this@PlayerInventoryComposition.y + midY }
            }
        }
    }
}
