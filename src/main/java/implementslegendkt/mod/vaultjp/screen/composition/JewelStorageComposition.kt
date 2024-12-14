package implementslegendkt.mod.vaultjp.screen.composition

import implementslegendkt.mod.screenlegends.Composition
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
import java.util.*

class JewelStorageComposition(
    private val firstSlot: Int,
    private val usefulnessCalculator: (ItemStack)-> Double,
    private val maxSize: ()->Int,
    private val screenID: ()->Int
) : Composition<JewelPurposerScreen> {
    private var jewelInvScroll = 0
    private var lastSortScreenId = -2
    private var pageCount = 1

    @JvmRecord
    data class OrderEntry(val slotPointer: Int, val usefulness: Double,val size:Int,val isAccepted: Boolean)

    private lateinit var jewelOrder: Array<OrderEntry?>
    private var dirty = true


    override fun JewelPurposerScreen.compose(midX: Int, midY: Int) {
        composeMainInv( midX, midY)
        composeSideDecorations( midX, midY)
    }

    private fun JewelPurposerScreen.composeMainInv(midX: Int, midY: Int) {
        background {
            texture = { ResourceLocation("vaultjp:textures/gui/jewel_inv.png") }
            srcRect = { Rect2i(0, 0, 158, 158) }
            pos = { midX + 20 to midY - 115 }
            atlasSize = { 158 to 158 }
        }


        repeat (64) {
            slotIndex->
            val x = slotIndex % 8
            val y = slotIndex / 8
            val slotCopy = slotIndex
            viewSlot {  //todo slot highlight
                val jewelIndex = {
                    jewelOrder[slotCopy + jewelInvScroll]!!.slotPointer
                }
                slot = { jewelIndex() + firstSlot }
                position = { x * 18 + 28 + midX to y * 18 - 107 + midY }
                mapItem = { itemStack ->
                    if (itemStack?.item is JewelItem)
                        appendUsefulnessToLore(
                            itemStack,
                            jewelOrder[slotCopy + jewelInvScroll]!!.usefulness
                        ) else itemStack
                }
                val isAccepted = {
                    jewelOrder[slotCopy + jewelInvScroll]!!.isAccepted
                }
                shouldHighlight = {
                    it || isAccepted()
                }
                highlightColor = {
                    when {
                        isAccepted() && it -> 0x7f77dd77
                        isAccepted() -> 0x7f339933
                        else -> -0x7f000001
                    }
                }
            }
        }
    }

    private fun JewelPurposerScreen.composeSideDecorations( midX: Int, midY: Int) {
        button {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            srcRect = { Rect2i(36, 0, 18, 18) }
            pos = { midX + 180 to midY - 108 }
            atlasSize = { 96 to 96 }
            onClick = {
                jewelInvScroll = Integer.max(0, jewelInvScroll - 64)
            }
        }
        text {
            text = { TextComponent(((jewelInvScroll + 64) / 64).toString() + "/" + pageCount) }
            pos = { width: Int -> midX + 190 - width / 2 to midY - 115 + 40 }
        }
        button {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            srcRect = { Rect2i(36, 18, 18, 18) }
            pos = { midX + 180 to midY - 115 + 61 }
            atlasSize = { 96 to 96 }
            onClick = {
                jewelInvScroll = Integer.min((pageCount - 1) * 64, jewelInvScroll + 64)
            }
        }
    }

    private fun appendUsefulnessToLore(item: ItemStack?, jewelUsefulness: Double): ItemStack? = item?.copy()?.apply {
        if(this.item !is JewelItem) return this
        getOrCreateTag().apply{
            put("display", getCompound("display").apply {
                put("Lore", getList("Lore", Tag.TAG_STRING.toInt()).apply {
                    addTag(0, StringTag.valueOf("\"Usefulness: $jewelUsefulness\""))
                })
            })
        }
    }

    override fun tick(screen: JewelPurposerScreen) { determineOrder(screen.menu.tileEntity) }

    fun markDirty() { dirty = true }

    fun determineOrder(tile: JewelPurposerBlockEntity) {
        val cont = tile.inventory.overSizedContents
        val currentScreenID = screenID()
        if (cont.isEmpty() || (currentScreenID == lastSortScreenId && !dirty)) return
        lastSortScreenId = currentScreenID

        var size = 0
        for (entry in cont) if ((!entry.overSizedStack().isEmpty) && (entry.overSizedStack().item is JewelItem)) size++

        pageCount = Integer.max(1, (size + 63) / 64)

        jewelOrder = arrayOfNulls(64 * pageCount)

        var orderIndex = 0
        repeat (cont.size) {contentIndex->
            if (cont[contentIndex].overSizedStack().item is JewelItem) {
                jewelOrder[orderIndex] = OrderEntry(contentIndex, usefulnessCalculator(cont[contentIndex].stack()),10,false)
                orderIndex++
            }
        }
        var contentIndex = 0
        while (contentIndex < cont.size && orderIndex < 64 * pageCount) {
            if (cont[contentIndex].overSizedStack().item !is JewelItem) {
                jewelOrder[orderIndex] = OrderEntry(contentIndex, usefulnessCalculator(ItemStack.EMPTY),10,false)
                orderIndex++
            }
            contentIndex++
        }

        jewelOrder.sortBy { -it!!.usefulness }
        val jewels = getJewels(tile).toSet()
        jewelOrder.indices.forEach {
            if(jewelOrder[it]!!.slotPointer in jewels)jewelOrder[it]=jewelOrder[it]!!.copy(isAccepted = true)
        }
        /*
        jewels.forEach {
            if(it>-1)
            jewelOrder[it]=jewelOrder[it]!!.copy(isAccepted = true)
        }*/
        dirty = false
    }

    fun getJewels(tile: JewelPurposerBlockEntity): IntArray {
        var size = 0
        val maxSize = maxSize()
        val jewels = IntArray(maxSize / 10)
        Arrays.fill(jewels, -1)
        var jewelCount = 0
        jewelOrder.forEach { entry->
            val jewel = tile.inventory.getItem(entry!!.slotPointer)
            if (jewel.item is JewelItem) {
                val data = VaultGearData.read(jewel)
                var jewelSize = data.get(
                    ModGearAttributes.JEWEL_SIZE,
                    VaultGearData.Type.ALL,
                    VaultGearAttributeTypeMerger.firstNonNull()
                )
                if (jewelSize == null) jewelSize = 0
                if (jewelSize + size > maxSize) return@forEach
                size += jewelSize
                jewels[jewelCount] = entry.slotPointer
                jewelCount++
                if (maxSize - size < 10 || jewelCount >= jewels.size) return jewels
            }
        }
        return jewels
    }
}
