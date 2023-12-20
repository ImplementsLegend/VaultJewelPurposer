package implementslegendkt.mod.vaultjp.screen.view;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import implementslegendkt.mod.vaultjp.screen.DecentScreen;
import implementslegendkt.mod.vaultjp.screen.View;
import implementslegendkt.mod.vaultjp.screen.ViewInteractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraft.network.chat.HoverEvent.Action.*;

public class TextViewDSL implements View {
    public Function<Integer, Pair<Integer,Integer>> pos = (width)->new Pair<>(0,0);
    public Supplier<Component> text = ()-> new TextComponent("");

    public static class TextInteractor implements ViewInteractor<TextViewDSL> {

        @Override
        public void clear() {
            views.clear();
        }
        private ArrayList<TextViewDSL> views = new ArrayList<>();
        @Override
        public void addView(TextViewDSL dsl) {
            views.add(dsl);
        }

        @Override
        public <S extends DecentScreen<S,?>> void renderViews(S screen, PoseStack stack, int cursorX, int cursorY) {
            for (var view:views) {
                var font = screen.getFont();
                var text = view.text.get();
                var width = font.width(text);
                var position = view.pos.apply(width);
                var height = font.lineHeight;
                font.draw(stack,text,position.getA(),position.getB(),0xffffff);
                var hover = text.getStyle().getHoverEvent();
                if(hover!=null){

                    var hoveringX = cursorX>position.getA() && cursorX< position.getA()+width;
                    var hoveringY = cursorY>position.getB() && cursorY< position.getB()+height;
                    if(hoveringX && hoveringY){
                        HoverEvent.Action<?> action = hover.getAction();
                        RenderSystem.disableDepthTest();
                        if (action.equals(SHOW_TEXT)) {
                            var txt = hover.getValue(SHOW_TEXT);
                            screen.renderTooltip(stack, List.of(txt), Optional.empty(),cursorX,cursorY);
                        } else if (action.equals(SHOW_ITEM)) {
                            var info = hover.getValue(SHOW_ITEM);
                            screen.renderTooltip(stack, screen.getTooltipFromItem(info.getItemStack()), info.getItemStack().getTooltipImage(), cursorX, cursorY);
                        } else if (action.equals(SHOW_ENTITY)) {
                            var info = hover.getValue(SHOW_ENTITY);
                            screen.renderTooltip(stack, info.getTooltipLines(), Optional.empty(), cursorX, cursorY);
                        }else { /*unknown tooltip type*/ }
                        RenderSystem.enableDepthTest();
                    }
                }
            }
        }

        @Override
        public <S extends DecentScreen<S,?>> void click(S screen, int cursorX, int cursorY,int key) {

        }

    }

}
