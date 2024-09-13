package implementslegendkt.mod.vaultjp.screen.composition

import implementslegendkt.mod.screenlegends.Composition
import implementslegendkt.mod.screenlegends.view.BackgroundViewDSL
import implementslegendkt.mod.screenlegends.view.ButtonViewDSL
import implementslegendkt.mod.screenlegends.view.SlotViewDSL
import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity
import implementslegendkt.mod.vaultjp.network.ApplyJewelsPacket
import implementslegendkt.mod.vaultjp.network.Channel
import implementslegendkt.mod.vaultjp.screen.JewelPurposerScreen
import iskallia.vault.item.tool.ToolItem
import net.minecraft.client.renderer.Rect2i
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import oshi.util.tuples.Pair
import java.util.function.Function

class ToolComposition(private val jewelListProvider: JewelPurposerBlockEntity.()->IntArray) :
    Composition<JewelPurposerScreen> {
    override fun compose(screen: JewelPurposerScreen, midX: Int, midY: Int) {
        screen.background {
            texture = { ResourceLocation("vaultjp:textures/gui/tool.png") }
            srcRect = { Rect2i(0, 0, 32, 68) }
            pos = { midX - 16 to midY - 40 }
            atlasSize = { 32 to 68 }
        }

        screen.viewSlot {
            slot = { screen.menu.slots.size - 1 }
            position = { midX - 8 to midY + 4 }
        }
        screen.button {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            srcRect = { Rect2i(36, 36, 18, 18) }
            atlasSize = { 96 to 96 }
            pos = { midX - 9 to midY - 15 }
            onClick = {
                val tile = screen.menu.tileEntity
                Channel.CHANNEL.sendToServer(ApplyJewelsPacket(tile.blockPos, tile.jewelListProvider()))
            }
        }
        screen.viewSlot {
            slot = { -1 }
            position = { midX - 8 to midY - 32 }
            shouldHighlight = { false }
            mapItem = itemMapper@{
                val tile = screen.menu.tileEntity
                val inv = tile.inventory
                val tool = inv.getItem(inv.containerSize - 1)
                return@itemMapper if (tool.item is ToolItem) tile.applyJewelsMock(tool, tile.jewelListProvider()) else ItemStack.EMPTY
            }
        }
    }
}
