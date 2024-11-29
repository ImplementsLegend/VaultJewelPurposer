package implementslegendkt.mod.vaultjp.screen.composition

import implementslegendkt.mod.screenlegends.Composition
import implementslegendkt.mod.screenlegends.view.BackgroundViewDSL
import implementslegendkt.mod.screenlegends.view.ButtonViewDSL
import implementslegendkt.mod.screenlegends.view.IntBoxViewDSL
import implementslegendkt.mod.screenlegends.view.TextViewDSL
import implementslegendkt.mod.vaultjp.AttributeUsefulness
import implementslegendkt.mod.vaultjp.JewelAttribute
import implementslegendkt.mod.vaultjp.JewelPurpose
import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity
import implementslegendkt.mod.vaultjp.screen.JewelPurposerScreen
import iskallia.vault.item.tool.JewelItem
import net.minecraft.client.renderer.Rect2i
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import oshi.util.tuples.Pair
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

class PurposeConfiguratorComposition(private val markDirty: ()->Unit) : Composition<JewelPurposerScreen> {
    private var currentPurposeIdx = 0
    var sizeLimit: Int = 300

    override fun compose(screen: JewelPurposerScreen, midX: Int, midY: Int) {
        composeSelector(screen, midX, midY)

        composeEntries(screen, midX, midY)
    }

    private fun composeSelector(screen: JewelPurposerScreen, midX: Int, midY: Int) {
        screen.button {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            atlasSize = { 96 to 96 }
            srcRect = { Rect2i(36, 54, 18, 18) }
            onClick = {
                markDirty()
                currentPurposeIdx = Integer.max(0, currentPurposeIdx - 1)
            }
            pos = { midX - 144 to midY - 122 }
        }


        screen.text {
            text = { TextComponent(if (screen.menu.tileEntity.purposes.isEmpty()) "_" else ("" + (currentPurposeIdx + 1))) }
            pos = { width: Int -> midX - 116 - width / 2 to midY - 116 }
        }
        screen.button {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            atlasSize = { 96 to 96 }
            srcRect = { Rect2i(0, 66, 18, 18) }
            onClick = {
                markDirty()
                currentPurposeIdx =
                    Integer.max(0, Integer.min(screen.menu.tileEntity.purposes.size - 1, currentPurposeIdx + 1))
            }
            pos = { midX - 107 to midY - 122 }
        }

        screen.button {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            atlasSize = { 96 to 96 }
            srcRect = { Rect2i(72, 36, 18, 18) }
            onClick = {
                val tile = screen.menu.tileEntity
                tile.purposes.add(JewelPurpose(ArrayList(), -1.0, true, "" + currentPurposeIdx))
                tile.syncToServer()
            }
            pos = { midX - 87 to midY - 122 }
        }
    }


    fun composeEntries(screen: JewelPurposerScreen, midX: Int, midY: Int) {
        screen.background {
            texture = { ResourceLocation("vaultjp:textures/gui/jewel_cfg.png") }
            srcRect = { Rect2i(0, 0, 186, 170) }
            pos = { midX - 206 to midY - 128 }
            atlasSize = { 186 to 170 }
        }
        val tile = screen.menu.tileEntity
        var i = 0
        for (p in JewelAttribute.values()) {
            val offset = i
            composeEntry(
                screen,
                midX - 197,
                midY + offset - 96,
                p.translationKey,
                { getAttrUsefulness(tile, p) },
                { newValue ->
                    markDirty()
                    modifyUsefulness(tile, p) { newValue }
                },
                p.normalization
            ) //screen.getAttrUsefulness(p));
            i += 12
        }
        composeEntry(
            screen,
            midX - 197,
            midY + i - 96,
            "vaultjp.config_entry.usefulness",
            { if (tile.purposes.isEmpty()) Double.NaN else currentPurpose(tile).disposeThreshold },
            { newValue -> modifyDisposeThreshold(tile) { newValue } },
            1.0
        ) //screen.getAttrUsefulness(p));
        i += 12
        composeEntry(
            screen,
            midX - 197,
            midY + i - 96,
            "vaultjp.config_entry.max_size",
            { sizeLimit.toDouble() },
            { markDirty(); sizeLimit = it.toInt() },
            1.0
        ) //screen.getAttrUsefulness(p));
        i += 12
    }

    fun composeEntry(
        screen: JewelPurposerScreen,
        x: Int,
        y: Int,
        label: String,
        valueGetter: Supplier<Double>,
        valueSetter: Consumer<Double>,
        normalization: Double
    ) {
        /*
        val buttonBase:ButtonViewDSL.()->Unit = {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            atlasSize = { 96 to 96 }
        }*/
        screen.text {
            text = {
                TranslatableComponent("$label.label").withStyle {
                    it.withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT, TranslatableComponent(
                                "$label.hover"
                            )
                        )
                    )
                }
            }
            pos = { x to y }
        }
        screen.intBox {
            texture = { ResourceLocation("vaultjp:textures/gui/extra.png") }
            atlasSize = { 96 to 96 }
            srcRect = { Rect2i(0, 84, 72, 12) }
            pos = { x + 98 to y - 2 }

            textPos = { width -> x + 169 - width to y }


            this.valueGetter = { (normalization * valueGetter.get()).toInt() }
            this.valueSetter = { valueSetter.accept(it / normalization) }
        }
    }

    fun getJewelUsefulness(stack: ItemStack?, tile: JewelPurposerBlockEntity): Double =
        if (tile.purposes.isEmpty() || stack?.item !is JewelItem) Double.NaN else currentPurpose(tile).getJewelUsefulness(stack)


    private fun getAttrUsefulness(tile: JewelPurposerBlockEntity, p: JewelAttribute): Double {
        if (tile.purposes.isEmpty()) return Double.NaN
        val purpose = currentPurpose(tile)
        val idx = purpose.values.withIndex().firstOrNull{(_,purpose)->purpose.attribute==p}?.index?:purpose.values.size
        return if (idx in purpose.values.indices) purpose.values[idx].multiplier else 0.0
    }


    private fun modifyUsefulness(tile: JewelPurposerBlockEntity, p: JewelAttribute, mod: Function<Double, Double?>) {
        if (tile.purposes.isEmpty()) return
        val purpose = currentPurpose(tile)
        val idx = purpose.values.withIndex().firstOrNull{(_,purpose)->purpose.attribute==p}?.index?:purpose.values.size
        if (idx in purpose.values.indices) {
            val newUsefulness = mod.apply(purpose.values[idx].multiplier)
            purpose.values[idx] = AttributeUsefulness(p, newUsefulness!!)
        } else {
            val newUsefulness = mod.apply(0.0)
            purpose.values.add(AttributeUsefulness(p, newUsefulness!!))
        }
        tile.syncToServer()
    }

    private fun modifyDisposeThreshold(tile: JewelPurposerBlockEntity, mod: Function<Double, Double?>) {
        if (tile.purposes.isEmpty()) return
        val purpose = currentPurpose(tile)
        val newPurpose =
            JewelPurpose(purpose.values, mod.apply(purpose.disposeThreshold)!!, purpose.divideBySize, purpose.name)
        tile.purposes[currentPurposeIdx] = newPurpose
        tile.syncToServer()
    }

    fun currentPurpose(tile: JewelPurposerBlockEntity) = tile.purposes[currentPurposeIdx]
}
