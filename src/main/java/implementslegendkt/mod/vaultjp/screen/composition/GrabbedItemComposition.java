package implementslegendkt.mod.vaultjp.screen.composition;

import implementslegendkt.mod.vaultjp.screen.Composition;
import implementslegendkt.mod.vaultjp.screen.DecentScreen;
import net.minecraft.client.Minecraft;
import oshi.util.tuples.Pair;

public class GrabbedItemComposition<T extends DecentScreen<T,?>>  implements Composition<T> {
    @Override
    public void compose(T screen, int midX, int midY) {
        screen.viewSlot((dsl)->{
            dsl.shouldHighlight = (should)->false;
            dsl.mapItem=(nul)->screen.menu.getCarried();
            dsl.position=()->{

                int i = (int)(Minecraft.getInstance().mouseHandler.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getScreenWidth()-8);
                int j = (int)(Minecraft.getInstance().mouseHandler.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getScreenHeight()-8);
                return new Pair<>(i,j);
            };
        });
    }
}
