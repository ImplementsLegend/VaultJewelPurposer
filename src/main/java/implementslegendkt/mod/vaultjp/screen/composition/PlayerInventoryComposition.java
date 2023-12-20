package implementslegendkt.mod.vaultjp.screen.composition;

import implementslegendkt.mod.vaultjp.screen.Composition;
import implementslegendkt.mod.vaultjp.screen.DecentScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import oshi.util.tuples.Pair;

public record PlayerInventoryComposition<S extends DecentScreen<S, ?>>(int hotbarStart, int mainInvStart,int x, int y) implements Composition<S> {
    @Override
    public void compose(S screen, int midX, int midY) {

        screen.background((it)->{
            it.texture=()->new ResourceLocation("vaultjp:textures/gui/player_base_inv.png");
            it.srcRect=()->new Rect2i(0,0,176,90);
            it.atlasSize = ()->new Pair(176,90);
            it.pos =( )->new Pair(midX+this.x,midY+this.y);
        });


        for(var slot = 0;slot<27;slot++){
            final var x = slot%9;
            final var y = slot/9;
            final var slotCopy = slot;
            screen.viewSlot((dsl)->{
                dsl.slot=()->slotCopy+ mainInvStart;
                dsl.position=()->new Pair<>(x*18+8+this.x+midX,y*18+8+midY+this.y);

            });
        }
        for(var slot = 0;slot<9;slot++){
            final var x = slot;
            final var slotCopy = slot;
            screen.viewSlot((dsl)->{
                dsl.slot=()->slotCopy+ hotbarStart;
                dsl.position=()->new Pair<>(x*18+8+this.x+midX,66+this.y+midY);

            });
        }
    }

}
