package implementslegendkt.mod.vaultjp.screen.composition

import implementslegendkt.mod.screenlegends.Composition
import implementslegendkt.mod.screenlegends.view.BackgroundViewDSL
import implementslegendkt.mod.screenlegends.view.ButtonViewDSL
import implementslegendkt.mod.screenlegends.view.SlotViewDSL
import implementslegendkt.mod.screenlegends.view.TextViewDSL
import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity
import implementslegendkt.mod.vaultjp.screen.JewelPurposerScreen
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger
import iskallia.vault.gear.data.VaultGearData
import iskallia.vault.init.ModGearAttributes
import iskallia.vault.item.tool.JewelItem
import net.minecraft.client.renderer.Rect2i
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import oshi.util.tuples.Pair
import java.util.*
import java.util.function.Function
import java.util.function.IntSupplier

class JewelStorageComposition(
    private val firstSlot: Int,
    private val usefulnessCalculator: Function<ItemStack, Double>,
    private val maxSize: IntSupplier,
    private val screenID: IntSupplier
) : Composition<JewelPurposerScreen> {
    private var jewelInvScroll = 0
    private var lastSortScreenId = -2
    private var pageCount = 1

    @JvmRecord
    data class OrderEntry(val slotPointer: Int, val usefulness: Double)

    private lateinit var jewelOrder: Array<OrderEntry?>
    private var dirty = true


    override fun compose(screen: JewelPurposerScreen, midX: Int, midY: Int) {
        composeMainInv(screen, midX, midY)
        composeSideDecorations(screen, midX, midY)
    }

    private fun composeMainInv(screen: JewelPurposerScreen, midX: Int, midY: Int) {
        screen.background {
            texture = { ResourceLocation("vaultjp:textures/gui/jewel_inv.png") }
            srcRect = { Rect2i(0, 0, 158, 158) }
            pos = { midX + 20 to midY - 115 }
            atlasSize = { 158 to 158 }
        }


        for (slotIndex in 0..63) {
            val x = slotIndex % 8
            val y = slotIndex / 8
            val slotCopy = slotIndex
            screen.viewSlot {  //todo slot highlight
                slot = { jewelOrder[slotCopy + jewelInvScroll]!!.slotPointer + firstSlot }
                position = { x * 18 + 28 + midX to y * 18 - 107 + midY }
                mapItem = { item ->
                    if(item?.item is JewelItem )
                    appendUsefulnessToLore(
                        item,
                        jewelOrder[slotCopy + jewelInvScroll]!!.usefulness
                    ) else item
                }
            }
        }
    }

    private fun composeSideDecorations(screen: JewelPurposerScreen, midX: Int, midY: Int) {
        screen.button {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            srcRect = { Rect2i(36, 0, 18, 18) }
            pos = { midX + 180 to midY - 108 }
            atlasSize = { 96 to 96 }
            onClick = {
                jewelInvScroll = Integer.max(0, jewelInvScroll - 64)
            }
        }
        screen.text {
            text = { TextComponent(((jewelInvScroll + 64) / 64).toString() + "/" + pageCount) }
            pos = { width: Int -> midX + 190 - width / 2 to midY - 115 + 40 }
        }
        screen.button {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            srcRect = { Rect2i(36, 18, 18, 18) }
            pos = { midX + 180 to midY - 115 + 61 }
            atlasSize = { 96 to 96 }
            onClick = {
                jewelInvScroll = Integer.min((pageCount - 1) * 64, jewelInvScroll + 64)
            }
        }
    }

    private fun appendUsefulnessToLore(item: ItemStack?, jewelUsefulness: Double): ItemStack? {
        val t0 = (item?.takeIf { it.item is JewelItem }?:return item).getOrCreateTag()
        val display = t0.getCompound("display")
        val lore = display.getList("Lore", Tag.TAG_STRING.toInt())
        lore.addTag(0, StringTag.valueOf("\"Usefulness: $jewelUsefulness\""))
        display.put("Lore", lore)
        val item2 = item.copy()
        item2.getOrCreateTag().put("display", display)
        return item2
    }

    override fun tick(screen: JewelPurposerScreen) {
        determineOrder(screen.menu.tileEntity)
    }

    fun markDirty() {
        dirty = true
    }

    fun determineOrder(tile: JewelPurposerBlockEntity) {
        val cont = tile.inventory.overSizedContents
        val currentScreenID = screenID.asInt
        if (cont.isEmpty() || (currentScreenID == lastSortScreenId && !dirty)) return
        lastSortScreenId = currentScreenID

        var size = 0
        for (entry in cont) if ((!entry.overSizedStack().isEmpty) && (entry.overSizedStack().item is JewelItem)) size++

        pageCount = Integer.max(1, (size + 63) / 64)

        jewelOrder = arrayOfNulls(64 * pageCount)

        var orderIndex = 0
        for (contentIndex in cont.indices) {
            if (cont[contentIndex].overSizedStack().item is JewelItem) {
                jewelOrder[orderIndex] =
                    OrderEntry(contentIndex, usefulnessCalculator.apply(cont[contentIndex].stack()))
                orderIndex++
            }
        }
        var contentIndex = 0
        while (contentIndex < cont.size && orderIndex < 64 * pageCount) {
            if (cont[contentIndex].overSizedStack().item !is JewelItem) {
                jewelOrder[orderIndex] = OrderEntry(contentIndex, usefulnessCalculator.apply(ItemStack.EMPTY))
                orderIndex++
            }
            contentIndex++
        }

        Arrays.sort(jewelOrder, Comparator.comparingDouble { it: OrderEntry? -> -it!!.usefulness })
        dirty = false
    }

    fun getJewels(tile: JewelPurposerBlockEntity): IntArray {
        var size = 0
        val maxSize = maxSize.asInt
        val jewels = IntArray(maxSize / 10)
        Arrays.fill(jewels, -1)
        var jewelCount = 0
        for (entry in jewelOrder) {
            val jewel = tile.inventory.getItem(entry!!.slotPointer)
            if (jewel.item is JewelItem) {
                val data = VaultGearData.read(jewel)
                var jewelSize = data.get(
                    ModGearAttributes.JEWEL_SIZE,
                    VaultGearData.Type.ALL,
                    VaultGearAttributeTypeMerger.firstNonNull()
                )
                if (jewelSize == null) jewelSize = 0
                if (jewelSize + size > maxSize) continue
                size += jewelSize
                jewels[jewelCount] = entry.slotPointer
                jewelCount++
                if (maxSize - size < 10 || jewelCount >= jewels.size) break
            }
        }
        return jewels
    }
}
