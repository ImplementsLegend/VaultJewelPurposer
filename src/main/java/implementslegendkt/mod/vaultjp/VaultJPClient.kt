package implementslegendkt.mod.vaultjp

import implementslegendkt.mod.vaultjp.screen.JewelPurposerScreen
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory


//@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
object VaultJPClient {
    fun registerScreen() {
        val screenConstructor = ScreenConstructor { a: JewelPurposerContainer?, b: Inventory?, c: Component? ->
            JewelPurposerScreen(
                a!!, c
            )
        }
        MenuScreens.register(JewelPurposerContainer.MENU_TYPE, screenConstructor)
    }
}
