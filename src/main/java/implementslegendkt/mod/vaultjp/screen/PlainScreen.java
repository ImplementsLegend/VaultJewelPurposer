package implementslegendkt.mod.vaultjp.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public class PlainScreen<M extends AbstractContainerMenu> extends DecentScreen<PlainScreen<M>,M> {


    private final List<Composition<PlainScreen<M>>> compositions;

    protected PlainScreen(M menu, Component p_96550_, List<Composition<PlainScreen<M>>> compositions) {
        super(menu, p_96550_);
        this.compositions = compositions;
    }
}
