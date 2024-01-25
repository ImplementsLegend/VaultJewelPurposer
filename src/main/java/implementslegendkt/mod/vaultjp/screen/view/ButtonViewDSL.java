package implementslegendkt.mod.vaultjp.screen.view;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import implementslegendkt.mod.vaultjp.screen.DecentScreen;
import implementslegendkt.mod.vaultjp.screen.View;
import implementslegendkt.mod.vaultjp.screen.ViewInteractor;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class ButtonViewDSL implements View {

    private boolean clicked = false;
    public boolean isClicked(){return clicked;}
    public Supplier<ResourceLocation> texture;
    public Supplier<Rect2i> srcRect;
    public Supplier<Pair<Integer,Integer>> atlasSize;
    public Supplier<Pair<Integer,Integer>> pos = ()->new Pair<>(0,0);
    public Runnable onClick = ()->{};

    public Function<Boolean, Boolean> shouldHighlight = (hovering)->hovering;

    public static class ButtonInteractor implements ViewInteractor<ButtonViewDSL> {

        @Override
        public void clear() {
            views.clear();
        }
        private ArrayList<ButtonViewDSL> views = new ArrayList<>();
        @Override
        public void addView(ButtonViewDSL dsl) {
            views.add(dsl);
        }

        @Override
        public <S extends DecentScreen<S,?>> void renderViews(S screen, PoseStack stack, int cursorX, int cursorY) {
            for (var view:views) {
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
        }

        @Override
        public <S extends DecentScreen<S,?>> void click(S screen, int cursorX, int cursorY,int key) {
            for (var view:views) {
                var position = view.pos.get();
                var srcrect = view.srcRect.get();
                if (cursorX>position.getA() && cursorX< position.getA()+srcrect.getWidth() && cursorY>position.getB() && cursorY< position.getB()+srcrect.getHeight()){
                    //view.clicked=true;
                    view.onClick.run();
                }else{
                    //view.clicked=false;
                }


            }

        }

    }

}
