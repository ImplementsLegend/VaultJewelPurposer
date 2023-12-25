package implementslegendkt.mod.vaultjp.screen;

import implementslegendkt.mod.vaultjp.JewelPurposerContainer;
import implementslegendkt.mod.vaultjp.screen.composition.*;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class JewelPurposerScreen extends DecentScreen<JewelPurposerScreen,JewelPurposerContainer> {
    private final JewelStorageComposition jewelStorage;
    private final PurposeConfiguratorComposition purposeConfigurator;
    private final ToolComposition tool;

    public JewelPurposerScreen(JewelPurposerContainer menu, Component p_96550_) {
        super(menu, p_96550_);
        var tile = menu.getTileEntity();
        purposeConfigurator = new PurposeConfiguratorComposition();
        jewelStorage = new JewelStorageComposition(36, (stack)->purposeConfigurator.getJewelUsefulness(stack, tile), () -> purposeConfigurator.sizeLimit);
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
