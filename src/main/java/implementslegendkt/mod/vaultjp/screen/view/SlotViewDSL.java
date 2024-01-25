package implementslegendkt.mod.vaultjp.screen.view;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import implementslegendkt.mod.vaultjp.screen.DecentScreen;
import implementslegendkt.mod.vaultjp.screen.View;
import implementslegendkt.mod.vaultjp.screen.ViewInteractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class SlotViewDSL implements View {

    public Supplier<Integer> slot = ()->-1;
    public Supplier<Pair<Integer,Integer>> position = ()->new Pair<>(0,0);
    public Function<ItemStack,ItemStack> mapItem = (stack)->stack;
    public Function<Boolean, Boolean> shouldHighlight = (hovering)->hovering;

    public static class SlotInteractor implements ViewInteractor<SlotViewDSL> {

        @Override
        public void clear() {
            views.clear();
        }
        private ArrayList<SlotViewDSL> views = new ArrayList<>();
        @Override
        public void addView(SlotViewDSL dsl) {
            views.add(dsl);
        }

        @Override
        public <S extends DecentScreen<S,?>> void renderViews(S screen, PoseStack stack, int cursorX, int cursorY) {
            for (var view:views) {
                var slot = view.slot.get();
                var itemStack = view.mapItem.apply((screen.menu.slots.size() > slot && slot>=0)?screen.menu.getSlot(slot).getItem():ItemStack.EMPTY);
                var position = view.position.get();
                renderFloatingItem(screen,itemStack, position.getA(), position.getB(), null);

                var hoveringX = cursorX>position.getA() && cursorX< position.getA()+16;
                var hoveringY = cursorY>position.getB() && cursorY< position.getB()+16;
                if(view.shouldHighlight.apply(hoveringX && hoveringY)){
                    RenderSystem.disableDepthTest();
                    GuiComponent.fill(stack, position.getA(),position.getB(), position.getA() + 16, position.getB() + 16, 0x80ffffff);
                    RenderSystem.enableDepthTest();
                }
                if (hoveringX && hoveringY && (screen.menu.getCarried().isEmpty() && !itemStack.isEmpty())){
                    RenderSystem.disableDepthTest();
                    screen.renderTooltip(stack, screen.getTooltipFromItem(itemStack), itemStack.getTooltipImage(), cursorX, cursorY);
                    RenderSystem.enableDepthTest();
                }
            }
        }

        @Override
        public <S extends DecentScreen<S,?>> void click(S screen, int cursorX, int cursorY,int key) {
            for (var view:views) {
                var slot = view.slot.get();
                //var itemStack = view.mapItem.apply((screen.menu.slots.size() > slot && slot>0)?screen.menu.getSlot(slot).getItem():ItemStack.EMPTY);
                var position = view.position.get();
                if (cursorX>position.getA() && cursorX< position.getA()+16){
                    if (cursorY>position.getB() && cursorY< position.getB()+16){
                        if(screen.menu.slots.size() > slot && slot>=0) {

                            boolean flag2 = slot != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                            ClickType clicktype = ClickType.PICKUP;
                            if (flag2) {
                                clicktype = ClickType.QUICK_MOVE;
                            } else if (slot == -999) {
                                clicktype = ClickType.THROW;
                            }
                            screen.getMinecraft().gameMode.handleInventoryMouseClick(screen.menu.containerId, slot, key, clicktype, screen.getMinecraft().player);
                            screen.menu.incrementStateId();

                        }
                    }
                }
            }

        }

        protected <S extends DecentScreen<S,?>> void renderFloatingItem(S screen,ItemStack p_97783_, int p_97784_, int p_97785_, String p_97786_) {
            RenderSystem.applyModelViewMatrix();
            screen.setBlitOffset(200);
            screen.getItemRenderer().blitOffset = 200.0F;
            net.minecraft.client.gui.Font font = net.minecraftforge.client.RenderProperties.get(p_97783_).getFont(p_97783_);
            if (font == null) font = screen.getFont();
            screen.getItemRenderer().renderAndDecorateItem(p_97783_, p_97784_, p_97785_);
            screen.getItemRenderer().renderGuiItemDecorations(font, p_97783_, p_97784_, p_97785_ - (screen.getDraggingItem().isEmpty() ? 0 : 8), p_97786_);
            screen.setBlitOffset(0);
            screen.getItemRenderer().blitOffset = 0.0F;
        }
    }

}
