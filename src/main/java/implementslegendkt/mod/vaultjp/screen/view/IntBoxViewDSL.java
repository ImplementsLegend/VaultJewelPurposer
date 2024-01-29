package implementslegendkt.mod.vaultjp.screen.view;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import implementslegendkt.mod.vaultjp.screen.DecentScreen;
import implementslegendkt.mod.vaultjp.screen.View;
import implementslegendkt.mod.vaultjp.screen.ViewInteractor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraft.network.chat.HoverEvent.Action.*;

public class IntBoxViewDSL implements View {

    public Supplier<ResourceLocation> texture;
    public Supplier<Rect2i> srcRect;
    public Supplier<Pair<Integer,Integer>> atlasSize;
    public Supplier<Pair<Integer,Integer>> pos = ()->new Pair<>(0,0);
    public Supplier<Integer> valueGetter = ()->0;
    public Consumer<Integer> valueSetter = (value)->{};
    public Supplier<Integer> maxValue = ()->1999999999;
    public Supplier<Integer> minValue = ()->-1999999999;
    public Function<Integer, Pair<Integer,Integer>> textPos = (width)->new Pair<>(0,0);
    public BiFunction<Integer,Integer,Component> text = (num,cursor)-> {
        if(cursor<0)return new TextComponent(num.toString());


        //this just underlines selected digit
        var str = num<0?num.toString().substring(1):num.toString();
        while (str.length()<cursor+1)str="0"+str;
        var tailStart = str.length()-cursor;
        var tail = tailStart<str.length()?str.substring(tailStart):"";
        var selected = str.substring(str.length()-1-cursor,str.length()-cursor);
        var headEnd = str.length()-1-cursor;
        var head = /*headEnd<0?"":*/(num<0?"-":"")+str.substring(0,headEnd);
        return new TextComponent(head).append(new TextComponent(selected).withStyle(ChatFormatting.UNDERLINE)).append(new TextComponent(tail));
    };

    public Function<Boolean, Boolean> shouldHighlight = (hovering)->hovering;

    public static class IntBoxInteractor implements ViewInteractor<IntBoxViewDSL> {

        private int selected = 0;
        private int cursor = 0;
        @Override
        public void clear() {
            views.clear();
        }
        private ArrayList<IntBoxViewDSL> views = new ArrayList<>();
        @Override
        public void addView(IntBoxViewDSL dsl) {
            views.add(dsl);
        }

        @Override
        public <S extends DecentScreen<S,?>> void renderViews(S screen, PoseStack stack, int cursorX, int cursorY) {
            for(var i = 0;i<views.size();i++){
                var view = views.get(i);
                {
                    //image
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShaderTexture(0, view.texture.get());
                    var rect = view.srcRect.get();
                    var position = view.pos.get();
                    var atlas = view.atlasSize.get();
                    screen.blit(stack, position.getA(), position.getB(), screen.getBlitOffset(), rect.getX(), rect.getY(),rect.getWidth(), rect.getHeight(),atlas.getA(),atlas.getB());

                    var hoveringX = cursorX>position.getA() && cursorX< position.getA()+rect.getWidth();
                    var hoveringY = cursorY>position.getB() && cursorY< position.getB()+rect.getHeight();
                    if(view.shouldHighlight.apply(hoveringX && hoveringY)){
                        RenderSystem.disableDepthTest();
                        GuiComponent.fill(stack, position.getA(),position.getB(), position.getA() + rect.getWidth(), position.getB() + rect.getHeight(), 0x40ffffff);
                        RenderSystem.enableDepthTest();
                    }
                }
                {
                    //text
                    var font = screen.getFont();
                    var text = view.text.apply(view.valueGetter.get(), i == selected ? cursor : -1);
                    var width = font.width(text);
                    var position = view.textPos.apply(width);
                    var height = font.lineHeight;
                    font.draw(stack, text, position.getA(), position.getB(), 0xffffff);
                    var hover = text.getStyle().getHoverEvent();
                    if (hover != null) {

                        var hoveringX = cursorX > position.getA() && cursorX < position.getA() + width;
                        var hoveringY = cursorY > position.getB() && cursorY < position.getB() + height;
                        if (hoveringX && hoveringY) {
                            HoverEvent.Action<?> action = hover.getAction();
                            RenderSystem.disableDepthTest();
                            if (action.equals(SHOW_TEXT)) {
                                var txt = hover.getValue(SHOW_TEXT);
                                screen.renderTooltip(stack, List.of(txt), Optional.empty(), cursorX, cursorY);
                            } else if (action.equals(SHOW_ITEM)) {
                                var info = hover.getValue(SHOW_ITEM);
                                screen.renderTooltip(stack, screen.getTooltipFromItem(info.getItemStack()), info.getItemStack().getTooltipImage(), cursorX, cursorY);
                            } else if (action.equals(SHOW_ENTITY)) {
                                var info = hover.getValue(SHOW_ENTITY);
                                screen.renderTooltip(stack, info.getTooltipLines(), Optional.empty(), cursorX, cursorY);
                            } else { /*unknown tooltip type*/ }
                            RenderSystem.enableDepthTest();
                        }
                    }
                }
            }
        }

        @Override
        public <S extends DecentScreen<S,?>> void click(S screen, int cursorX, int cursorY,int key) {
            for(var i = 0;i<views.size();i++){
                var view = views.get(i);
                var position = view.pos.get();
                var rect = view.srcRect.get();
                var width = rect.getWidth();
                var height = rect.getHeight();

                var hoveringX = cursorX>position.getA() && cursorX< position.getA()+width;
                var hoveringY = cursorY>position.getB() && cursorY< position.getB()+height;
                if(hoveringX && hoveringY){
                    selected=i;
                }
            }

        }

        @Override
        public <S extends DecentScreen<S, ?>> void type(S screen, int glfwKey, int modifiers) {
            var view = views.get(selected);
            if(glfwKey == GLFW.GLFW_KEY_TAB){
                selected=selected+(((modifiers&0b010)==0)?+1:-1);
                while (selected<0)selected+=views.size();
                while (selected>=views.size())selected-=views.size();
            }
            if(glfwKey == GLFW.GLFW_KEY_KP_ADD || glfwKey == GLFW.GLFW_KEY_UP){
                var newValue = view.valueGetter.get()+getCursorExp();
                if(newValue<view.maxValue.get())view.valueSetter.accept(newValue);
            }
            if(glfwKey == GLFW.GLFW_KEY_LEFT){
                cursor= Integer.min(cursor+1,(int)Math.log10(view.valueGetter.get()<0?-view.minValue.get():view.maxValue.get()));
            }
            if(glfwKey == GLFW.GLFW_KEY_RIGHT){
                cursor = Integer.max(cursor-1,0);
            }
            if(glfwKey == GLFW.GLFW_KEY_KP_SUBTRACT || glfwKey == GLFW.GLFW_KEY_DOWN){
                var newValue = view.valueGetter.get()-getCursorExp();
                if(newValue>view.minValue.get())view.valueSetter.accept(newValue);
            }
            if (glfwKey>=GLFW.GLFW_KEY_0 && glfwKey<=GLFW.GLFW_KEY_9){
                var digit = glfwKey-GLFW.GLFW_KEY_0;
                var value = view.valueGetter.get();
                var currentDigit = (value/getCursorExp())%10;
                var newValue = value-(currentDigit+((value<0)?digit:-digit))*getCursorExp();
                if(newValue<view.maxValue.get())view.valueSetter.accept(newValue);

            }
            if (glfwKey>=GLFW.GLFW_KEY_KP_0 && glfwKey<=GLFW.GLFW_KEY_KP_9){
                var digit = glfwKey-GLFW.GLFW_KEY_KP_0;
                var value = view.valueGetter.get();
                var currentDigit = (value/getCursorExp())%10;
                var newValue = value-(currentDigit+((value<0)?digit:-digit))*getCursorExp();
                if(newValue<view.maxValue.get())view.valueSetter.accept(newValue);
            }
        }
        private int getCursorExp(){
            var result = 1;
            for (int i = 0; i < cursor; i++) {
                result *=10;
            }
            return result;
        }
    }

}
