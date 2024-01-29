package implementslegendkt.mod.vaultjp.screen;

import implementslegendkt.mod.vaultjp.JewelPurpose;
import implementslegendkt.mod.vaultjp.JewelPurposerContainer;
import implementslegendkt.mod.vaultjp.screen.composition.*;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JewelPurposerScreen extends DecentScreen<JewelPurposerScreen,JewelPurposerContainer> {
    private final JewelStorageComposition jewelStorage;
    private final PurposeConfiguratorComposition purposeConfigurator;
    private final ToolComposition tool;

    public JewelPurposerScreen(JewelPurposerContainer menu, Component p_96550_) {
        super(menu, p_96550_);
        var tile = menu.getTileEntity();
        var thiz = this;

        if(tile.purposes.isEmpty()){

            tile.purposes.add(new JewelPurpose(new ArrayList<>(),-1.0,true,""+0));
            tile.syncToServer();
        }

        purposeConfigurator = new PurposeConfiguratorComposition(()->thiz.jewelStorage.markDirty());
        jewelStorage = new JewelStorageComposition(36, (stack)->thiz.purposeConfigurator.getJewelUsefulness(stack, tile), () -> thiz.purposeConfigurator.sizeLimit, menu::getStateId);
        jewelStorage.determineOrder(tile);
        tool = new ToolComposition(jewelStorage::getJewels);
    }
    @Override
    protected List<Composition<JewelPurposerScreen>> createCompositions() {
        return Arrays.asList(
                new PlayerInventoryComposition<>(27, 0, -176 / 2, 52),
                jewelStorage,
                purposeConfigurator,
                tool,
                new GrabbedItemComposition<>()
        );
    }
}
