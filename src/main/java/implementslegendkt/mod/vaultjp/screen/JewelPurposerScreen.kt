package implementslegendkt.mod.vaultjp.screen

import implementslegendkt.mod.screenlegends.Composition
import implementslegendkt.mod.screenlegends.DecentScreen
import implementslegendkt.mod.vaultjp.JewelPurpose
import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity
import implementslegendkt.mod.vaultjp.JewelPurposerContainer
import implementslegendkt.mod.vaultjp.screen.composition.*
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.util.*

class JewelPurposerScreen(menu: JewelPurposerContainer, p_96550_: Component?) :
    DecentScreen<JewelPurposerScreen, JewelPurposerContainer>(menu, p_96550_) {
    private val jewelStorage: JewelStorageComposition
    private val purposeConfigurator: PurposeConfiguratorComposition
    private val tool: ToolComposition

    init {
        val tile = menu.tileEntity
        val thiz = this

        tile.takeIf { it.purposes.isEmpty() }?.apply {
            purposes.add(JewelPurpose(arrayListOf(),-1.0,true,0.toString()))
            syncToServer()
        }
        purposeConfigurator = PurposeConfiguratorComposition { thiz.jewelStorage.markDirty() }
        jewelStorage = JewelStorageComposition(
            36,
            { stack: ItemStack? -> thiz.purposeConfigurator.getJewelUsefulness(stack, tile) },
            { thiz.purposeConfigurator.sizeLimit },
            { menu.stateId })
        jewelStorage.determineOrder(tile)
        tool = ToolComposition { jewelStorage.getJewels(this@ToolComposition) }
    }

    override fun createCompositions(): List<Composition<JewelPurposerScreen>> = listOf(
        PlayerInventoryComposition(27, 0, -176 / 2, 52),
        jewelStorage,
        purposeConfigurator,
        tool,
        GrabbedItemComposition()
    )
}
