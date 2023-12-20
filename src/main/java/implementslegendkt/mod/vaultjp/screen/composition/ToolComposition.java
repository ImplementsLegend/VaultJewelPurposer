package implementslegendkt.mod.vaultjp.screen.composition;

import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity;
import implementslegendkt.mod.vaultjp.network.ApplyJewelsPacket;
import implementslegendkt.mod.vaultjp.network.Channel;
import implementslegendkt.mod.vaultjp.screen.Composition;
import implementslegendkt.mod.vaultjp.screen.JewelPurposerScreen;
import iskallia.vault.item.tool.ToolItem;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.function.Function;

public class ToolComposition implements Composition<JewelPurposerScreen> {

    private final Function<JewelPurposerBlockEntity,int[]> jewelListProvider;

    public ToolComposition(Function<JewelPurposerBlockEntity, int[]> jewelListProvider) {
        this.jewelListProvider = jewelListProvider;
    }

    @Override
    public void compose(JewelPurposerScreen screen, int midX, int midY) {

        screen.background((it)->{
            it.texture=()->new ResourceLocation("vaultjp:textures/gui/tool.png");
            it.srcRect=()->new Rect2i(0,0,32,68);
            it.pos = ()->new Pair<>(midX-16,midY-40);
            it.atlasSize = ()->new Pair<>(32,68);
        });

        screen.viewSlot((dsl)-> {
            dsl.slot=()->screen.menu.slots.size()-1;
            dsl.position = ()->new Pair<>(midX-8,midY+4);
        });
        screen.button((dsl)->{
            dsl.texture=()->new ResourceLocation("vaultjp:textures/gui/extra.png");
            dsl.srcRect=()->new Rect2i(36,36,18,18);
            dsl.atlasSize = ()->new Pair<>(96,96);
            dsl.pos = ()->new Pair<>(midX-9,midY-15);
            dsl.onClick=()-> {
                var tile = screen.menu.getTileEntity();
                Channel.CHANNEL.sendToServer(new ApplyJewelsPacket(tile.getBlockPos(), jewelListProvider.apply(tile)));
            };

        });
        screen.viewSlot((dsl)-> {
            dsl.slot=()->-1;
            dsl.position = ()->new Pair<>(midX-8,midY-32);
            dsl.shouldHighlight = (unused)->false;
            dsl.mapItem=(empty)->{
                var tile = screen.menu.getTileEntity();
                var inv = tile.getInventory();
                var tool = inv.getItem(inv.getContainerSize()-1);
                if(tool.getItem() instanceof ToolItem){

                    //todo
                    return tile.applyJewelsMock(tool, jewelListProvider.apply(tile));
                }else return ItemStack.EMPTY;
            };
        });
    }
}
