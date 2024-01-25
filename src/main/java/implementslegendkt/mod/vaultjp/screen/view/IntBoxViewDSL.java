package implementslegendkt.mod.vaultjp.screen.view;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import implementslegendkt.mod.vaultjp.screen.DecentScreen;
import implementslegendkt.mod.vaultjp.screen.View;
import implementslegendkt.mod.vaultjp.screen.ViewInteractor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.network.chat.HoverEvent.Action.*;

public class IntBoxViewDSL implements View {
    public int number = 0; //todo replace with getter/setter
    public Function<Integer, Pair<Integer,Integer>> pos = (width)->new Pair<>(0,0);
    public BiFunction<Integer,Integer,Component> text = (num,cursor)-> {
        if(cursor<0)return new TextComponent(num.toString());
        var str = num.toString();
        while (str.length()<cursor+1)str="0"+str;
        var tailStart = str.length()-cursor;
        var tail = tailStart<str.length()?str.substring(tailStart):"";
        var selected = str.substring(str.length()-1-cursor,str.length()-cursor);
        var headEnd = str.length()-1-cursor;
        var head = /*headEnd<0?"":*/str.substring(0,headEnd);
        return new TextComponent(head).append(new TextComponent(selected).withStyle(ChatFormatting.UNDERLINE)).append(new TextComponent(tail));
    };

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
                var font = screen.getFont();
                var text = view.text.apply(view.number,i==selected?cursor:-1);
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
            for(var i = 0;i<views.size();i++){
                var view = views.get(i);
                var font = screen.getFont();
                var text = view.text.apply(view.number,i==selected?cursor:-1);
                var width = font.width(text);
                var position = view.pos.apply(width);
                var height = font.lineHeight;

                var hoveringX = cursorX>position.getA() && cursorX< position.getA()+width;
                var hoveringY = cursorY>position.getB() && cursorY< position.getB()+height;
                if(hoveringX && hoveringY){
                    selected=i;
                }
            }

        }

        @Override
        public <S extends DecentScreen<S, ?>> void type(S screen, int glfwKey, int modifiers) {
            if(glfwKey == GLFW.GLFW_KEY_TAB){
                selected=selected+(((modifiers&0b010)==0)?+1:-1);
                while (selected<0)selected+=views.size();
                while (selected>=views.size())selected-=views.size();
            }
            if(glfwKey == GLFW.GLFW_KEY_KP_ADD || glfwKey == GLFW.GLFW_KEY_UP){
                views.get(selected).number+=getCursorExp();
            }
            if(glfwKey == GLFW.GLFW_KEY_LEFT){
                cursor++;
            }
            if(glfwKey == GLFW.GLFW_KEY_RIGHT){
                cursor--;
            }
            if(glfwKey == GLFW.GLFW_KEY_KP_SUBTRACT || glfwKey == GLFW.GLFW_KEY_DOWN){
                views.get(selected).number-=getCursorExp();
            }
            if (glfwKey>=GLFW.GLFW_KEY_0 && glfwKey<=GLFW.GLFW_KEY_9){
                var digit = glfwKey-GLFW.GLFW_KEY_0;
                var currentDigit = (views.get(selected).number/getCursorExp())%10;
                views.get(selected).number+=(digit-currentDigit)*getCursorExp();

            }
            if (glfwKey>=GLFW.GLFW_KEY_KP_0 && glfwKey<=GLFW.GLFW_KEY_KP_9){
                var digit = glfwKey-GLFW.GLFW_KEY_KP_0;
                var currentDigit = (views.get(selected).number/getCursorExp())%10;
                views.get(selected).number+=(digit-currentDigit)*getCursorExp();
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
