package implementslegendkt.mod.vaultjp.screen.composition;

import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity;
import implementslegendkt.mod.vaultjp.JewelPurposerContainer;
import implementslegendkt.mod.vaultjp.screen.Composition;
import implementslegendkt.mod.vaultjp.screen.JewelPurposerScreen;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

public class JewelStorageComposition implements Composition<JewelPurposerScreen> {


    private final int firstSlot;
    private final Function<ItemStack,Double> usefulnessCalculator;
    private final Supplier<Integer> maxSize;
    private int jewelInvScroll = 0;
    private int pageCount = 1;

    public static record OrderEntry(int slotPointer, double usefulness){}
    private OrderEntry[] jewelOrder;
    public JewelStorageComposition(int firstSlot, Function<ItemStack, Double> usefulness, Supplier<Integer> maxSize) {
        this.firstSlot = firstSlot;
        this.usefulnessCalculator = usefulness;
        this.maxSize = maxSize;
    }

    @Override
    public void compose(JewelPurposerScreen screen, int midX, int midY) {

        composeMainInv(screen,midX,midY);
        composeSideDecorations(screen,midX,midY);
    }

    private void composeMainInv(JewelPurposerScreen screen, int midX, int midY){
        screen.background((it)->{
            it.texture=()->new ResourceLocation("vaultjp:textures/gui/jewel_inv.png");
            it.srcRect=()->new Rect2i(0,0,158,158);
            it.pos = ()->new Pair<>(midX+20,midY-115);
            it.atlasSize = ()->new Pair<>(158,158);
        });


        for(var slot = 0;slot<64;slot++){
            final var x = slot%8;
            final var y = slot/8;
            final var slotCopy = slot;
            screen.viewSlot((dsl)->{
                dsl.slot=()->jewelOrder[slotCopy+jewelInvScroll].slotPointer()+ firstSlot;
                dsl.position=()->new Pair<>(x*18+28+midX,y*18-107+midY);
                dsl.mapItem= item -> appendUsefulnessToLore(item, jewelOrder[slotCopy+jewelInvScroll].usefulness);
            });
        }
    }

    private void composeSideDecorations(JewelPurposerScreen screen, int midX, int midY) {

        screen.button((dsl)->{
            dsl.texture=()->new ResourceLocation("vaultjp:textures/gui/extra.png");
            dsl.srcRect=()->new Rect2i(36,0,18,18);
            dsl.pos = ()->new Pair<>(midX+180,midY-108);
            dsl.atlasSize = ()->new Pair<>(96,96);
            dsl.onClick = ()->{
                jewelInvScroll=Integer.max(0,jewelInvScroll-64);
            };
        });
        screen.text((dsl)->{
            dsl.text = ()->new TextComponent( (jewelInvScroll+64)/64+"/"+pageCount);
            dsl.pos = (width)->new Pair<>(midX+190-width/2,midY-115+40);
        });
        screen.button((dsl)->{
            dsl.texture=()->new ResourceLocation("vaultjp:textures/gui/extra.png");
            dsl.srcRect=()->new Rect2i(36,18,18,18);
            dsl.pos = ()->new Pair<>(midX+180,midY-115+61);
            dsl.atlasSize = ()->new Pair<>(96,96);
            dsl.onClick = ()->{
                jewelInvScroll=Integer.min((pageCount-1)*64,jewelInvScroll+64);
            };
        });

    }

    private ItemStack appendUsefulnessToLore(ItemStack item, double jewelUsefulness) {
        var t0 =item.getOrCreateTag();
        var display = t0.getCompound("display");
        var lore = display.getList("Lore", Tag.TAG_STRING);
        lore.addTag(0, StringTag.valueOf("\"Usefulness: "+jewelUsefulness+"\""));
        display.put("Lore",lore);
        var item2 = item.copy();
        item2.getOrCreateTag().put("display",display);
        return item2;
    }

    @Override
    public void tick(JewelPurposerScreen screen) {
        determineOrder(screen.menu.getTileEntity());
    }

    public void determineOrder(JewelPurposerBlockEntity tile) {

        var cont = tile.getInventory().getOverSizedContents();
        if(cont.isEmpty())return;

        var size = 0;
        for (var entry:cont) if((!entry.overSizedStack().isEmpty()) &&(entry.overSizedStack().getItem() instanceof JewelItem))size++;

        pageCount=Integer.max(1,(size+63)/64);
        jewelOrder = new OrderEntry[64*pageCount];

        var orderIndex = 0;
        for (int contentIndex = 0; contentIndex < cont.size(); contentIndex++) {
            if(cont.get(contentIndex).overSizedStack().getItem() instanceof JewelItem){
                jewelOrder[orderIndex]=new OrderEntry(contentIndex, usefulnessCalculator.apply(cont.get(contentIndex).stack()));
                orderIndex++;
            }
        }
        for (int contentIndex = 0; contentIndex < cont.size() && orderIndex<64*pageCount; contentIndex++) {
            if(!(cont.get(contentIndex).overSizedStack().getItem() instanceof JewelItem)){
                jewelOrder[orderIndex]=new OrderEntry(contentIndex,-1);
                orderIndex++;
            }
        }

        Arrays.sort(jewelOrder, Comparator.comparingDouble((it)->-it.usefulness()));
    }



    public int[] getJewels(JewelPurposerBlockEntity tile) {
        var size = 0;
        var maxSize = this.maxSize.get();
        var jewels = new int[maxSize/ 10];
        Arrays.fill(jewels, -1);
        var jewelCount = 0;
        for (var entry : jewelOrder) {
            var jewel = tile.getInventory().getItem(entry.slotPointer());
            if (jewel.getItem() instanceof JewelItem) {
                var data = VaultGearData.read(jewel);
                var jewelSize = data.get(ModGearAttributes.JEWEL_SIZE, VaultGearData.Type.ALL,  VaultGearAttributeTypeMerger.firstNonNull());
                if(jewelSize==null) jewelSize=0;
                if (jewelSize + size > maxSize) continue;
                size += jewelSize;
                jewels[jewelCount] = entry.slotPointer();
                jewelCount++;
                if (maxSize - size < 10 || jewelCount >= jewels.length) break;
            }
        }
        return jewels;

    }
}
