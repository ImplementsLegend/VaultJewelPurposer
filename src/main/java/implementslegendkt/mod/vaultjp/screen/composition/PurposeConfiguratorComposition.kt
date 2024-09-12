package implementslegendkt.mod.vaultjp.screen.composition;

import implementslegendkt.mod.vaultjp.AttributeUsefulness;
import implementslegendkt.mod.vaultjp.JewelAttribute;
import implementslegendkt.mod.vaultjp.JewelPurpose;
import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity;
import implementslegendkt.mod.vaultjp.screen.Composition;
import implementslegendkt.mod.vaultjp.screen.JewelPurposerScreen;
import implementslegendkt.mod.vaultjp.screen.view.ButtonViewDSL;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PurposeConfiguratorComposition implements Composition<JewelPurposerScreen> {


    private final Runnable markDirty;
    private int currentPurposeIdx =0;
    public int sizeLimit=300;

    public PurposeConfiguratorComposition(Runnable markDirty) {
        this.markDirty = markDirty;
    }

    @Override
    public void compose(JewelPurposerScreen screen, int midX, int midY) {

        composeSelector(screen,midX,midY);

        composeEntries(screen,midX,midY);
    }

    private void composeSelector(JewelPurposerScreen screen, int midX, int midY) {

        screen.button((dsl)-> {
            dsl.texture = () -> new ResourceLocation("vaultjp:textures/gui/extra.png");
            dsl.atlasSize = () -> new Pair<>(96, 96);
            dsl.srcRect = () -> new Rect2i(36, 54, 18, 18);
            dsl.onClick = ()-> {
                markDirty.run();
                currentPurposeIdx = Integer.max(0, currentPurposeIdx - 1);
            };
            dsl.pos = () -> new Pair<>(midX - 144, midY - 122);
        });


        screen.text((dsl)->{
            dsl.text=()->new TextComponent(screen.menu.getTileEntity().purposes.isEmpty()?"_":(""+(currentPurposeIdx+1)));
            dsl.pos = (width) -> new Pair<>(midX-116-width/2, midY-116);
        });
        screen.button((dsl)-> {
            dsl.texture = () -> new ResourceLocation("vaultjp:textures/gui/extra.png");
            dsl.atlasSize = () -> new Pair<>(96, 96);
            dsl.srcRect = () -> new Rect2i(0, 66, 18, 18);
            dsl.onClick = ()-> {
                markDirty.run();
                currentPurposeIdx = Integer.max(0, Integer.min(screen.menu.getTileEntity().purposes.size() - 1, currentPurposeIdx + 1));
            };
            dsl.pos = () -> new Pair<>(midX - 107, midY - 122);
        });

        screen.button((dsl)-> {
            dsl.texture = () -> new ResourceLocation("vaultjp:textures/gui/extra.png");
            dsl.atlasSize = () -> new Pair<>(96, 96);
            dsl.srcRect = () -> new Rect2i(72, 36, 18, 18);
            dsl.onClick = ()->{
                var tile = screen.menu.getTileEntity();
                tile.purposes.add(new JewelPurpose(new ArrayList<>(),-1.0,true,""+currentPurposeIdx));
                tile.syncToServer();
            };
            dsl.pos = () -> new Pair<>(midX - 87, midY - 122);
        });
    }


    public void composeEntries(JewelPurposerScreen screen, int midX, int midY){


        screen.background((it)->{
            it.texture=()->new ResourceLocation("vaultjp:textures/gui/jewel_cfg.png");
            it.srcRect=()->new Rect2i(0,0,186,170);
            it.pos = ()->new Pair<>(midX-206,midY-128);
            it.atlasSize = ()->new Pair<>(186,170);
        });
        var tile = screen.menu.getTileEntity();
        var i = 0;
        for(var p : JewelAttribute.values()) {
            final var offset = i;
            composeEntry(screen,midX-197,midY+offset-96,p.translationKey(),()->getAttrUsefulness(tile, p),(newValue)-> {
                markDirty.run();
                modifyUsefulness(tile, p, (unused) -> newValue);
            },p.normalization);//screen.getAttrUsefulness(p));
            i += 12;
        }
        composeEntry(screen,midX-197,midY+i-96,"vaultjp.config_entry.usefulness",()->tile.purposes.isEmpty()? Double.NaN:currentPurpose(tile).disposeThreshold(),(newValue)->modifyDisposeThreshold(tile,(unused)->newValue),1.0);//screen.getAttrUsefulness(p));
        i += 12;
        composeEntry(screen,midX-197,midY+i-96,"vaultjp.config_entry.max_size",()->(double)sizeLimit,(newValue)->sizeLimit=newValue.intValue(),1.0);//screen.getAttrUsefulness(p));
        i += 12;

    }
    public void composeEntry(JewelPurposerScreen screen,int x, int y, String label, Supplier<Double> valueGetter, Consumer<Double> valueSetter, double normalization){

        Consumer<ButtonViewDSL> buttonBase = (dsl) -> {
            dsl.texture = () -> new ResourceLocation("vaultjp:textures/gui/extra.png");
            dsl.atlasSize = () -> new Pair<>(96, 96);
        };
        screen.text((dsl)->{
            dsl.text=()->new TranslatableComponent(label+".label").withStyle((style)->style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new TranslatableComponent(label+".hover"))));
            dsl.pos = (width) -> new Pair<>(x, y);
        });
        screen.intBox((dsl)->{

            dsl.texture = () -> new ResourceLocation("vaultjp:textures/gui/extra.png");
            dsl.atlasSize = () -> new Pair<>(96, 96);
            dsl.srcRect = () -> new Rect2i(0, 84, 72, 12);
            dsl.pos = () -> new Pair<>(x + 98, y-2);

            dsl.textPos =(width) -> new Pair<>(x+169-width, y);
            dsl.valueGetter=()->(int)(normalization*valueGetter.get());
            dsl.valueSetter=(it)->valueSetter.accept(it/normalization);
        });
        /*
        screen.button((dsl) -> {
            buttonBase.accept(dsl);
            dsl.onClick = ()-> valueSetter.accept(valueGetter.get()*0.1);
            dsl.srcRect = () -> new Rect2i(0, 38, 10, 10);
            dsl.pos = () -> new Pair<>(x+92, y-1);
        });
        screen.button((dsl) -> {
            buttonBase.accept(dsl);
            dsl.onClick = ()-> valueSetter.accept( valueGetter.get()-1);
            dsl.srcRect = () -> new Rect2i(0, 48, 10, 10);
            dsl.pos = () -> new Pair<>(x+103, y-1);
        });
        screen.text((dsl)->{
            dsl.text=()->new TextComponent("%.2f".formatted(valueGetter.get()));
            dsl.pos = (width) -> new Pair<>(x+114, y);
        });

        screen.button((dsl) -> {
            buttonBase.accept(dsl);
            dsl.onClick = ()-> valueSetter.accept( valueGetter.get()+1);
            dsl.srcRect = () -> new Rect2i(0, 18, 10, 10);
            dsl.pos = () -> new Pair<>(x+148, y-1);
        });
        screen.button((dsl) -> {
            buttonBase.accept(dsl);
            dsl.onClick = ()-> valueSetter.accept( valueGetter.get()*10);
            dsl.srcRect = () -> new Rect2i(0, 28, 10, 10);
            dsl.pos = () -> new Pair<>(x+159, y-1);
        });*/

    }

    public double getJewelUsefulness(ItemStack stack, JewelPurposerBlockEntity tile) {
        if(tile.purposes.isEmpty())return Double.NaN;
        else return currentPurpose(tile).getJewelUsefulness(stack);
    }


    private double getAttrUsefulness(JewelPurposerBlockEntity tile, JewelAttribute p) {
        if(tile.purposes.isEmpty())return Double.NaN;
        var idx = 0;
        var purpose = currentPurpose(tile);
        for(var x:purpose.values()){
            if(x.attribute()==p)break;
            idx++;
        }
        return purpose.values().size() > idx ? purpose.values().get(idx).multiplier() : 0.0;

    }


    private void modifyUsefulness(JewelPurposerBlockEntity tile, JewelAttribute p, Function<Double, Double> mod) {
        if (tile.purposes.isEmpty())return;
        var idx = 0;
        var purpose = currentPurpose(tile);
        for(var x:purpose.values()){
            if(x.attribute()==p)break;
            idx++;
        }
        if(purpose.values().size()>idx){
            var newUsefulness = mod.apply(purpose.values().get(idx).multiplier());
            purpose.values().set(idx,new AttributeUsefulness(p,newUsefulness));
        }else {

            var newUsefulness = mod.apply(0.0);
            purpose.values().add(new AttributeUsefulness(p,newUsefulness));
        }
        tile.syncToServer();
    }

    private void modifyDisposeThreshold(JewelPurposerBlockEntity tile, Function<Double, Double> mod) {
        if (tile.purposes.isEmpty())return;
        var purpose = currentPurpose(tile);
        var newPurpose = new JewelPurpose(purpose.values(),mod.apply(purpose.disposeThreshold()),purpose.divideBySize(), purpose.name());
        tile.purposes.set(currentPurposeIdx,newPurpose);
        tile.syncToServer();
    }
    public JewelPurpose currentPurpose(JewelPurposerBlockEntity tile) {
        return tile.purposes.get(currentPurposeIdx);
    }
}
