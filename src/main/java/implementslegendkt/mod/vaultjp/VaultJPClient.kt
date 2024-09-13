package implementslegendkt.mod.vaultjp;


import implementslegendkt.mod.vaultjp.screen.JewelPurposerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.common.Mod;

//@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class VaultJPClient {

    public static void registerScreen(){
        MenuScreens.ScreenConstructor<JewelPurposerContainer, JewelPurposerScreen> screenConstructor = (a, b, c) -> new JewelPurposerScreen(a, c);
        MenuScreens.register(JewelPurposerContainer.MENU_TYPE, screenConstructor);
    }
}
