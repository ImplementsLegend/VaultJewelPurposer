package implementslegendkt.mod.vaultjp.screen.composition

import implementslegendkt.mod.screenlegends.Composition
import implementslegendkt.mod.screenlegends.DecentScreen
import implementslegendkt.mod.screenlegends.view.SlotViewDSL
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import oshi.util.tuples.Pair

class GrabbedItemComposition<T : DecentScreen<T, *>> : Composition<T> {
    override fun T.compose(midX: Int, midY: Int) {
        viewSlot {
            shouldHighlight = { false }
            mapItem = { menu.carried }
            position = {
                val i =
                    (Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().window.guiScaledWidth.toDouble() / Minecraft.getInstance().window.screenWidth.toDouble() - 8).toInt()
                val j =
                    (Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().window.guiScaledHeight.toDouble() / Minecraft.getInstance().window.screenHeight.toDouble() - 8).toInt()
                i to j
            }
            zOffset={ 100f }
        }

    }
}
