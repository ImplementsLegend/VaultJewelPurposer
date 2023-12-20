package implementslegendkt.mod.vaultjp.screen.view;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import implementslegendkt.mod.vaultjp.screen.DecentScreen;
import implementslegendkt.mod.vaultjp.screen.View;
import implementslegendkt.mod.vaultjp.screen.ViewInteractor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.function.Supplier;

public class BackgroundViewDSL implements View {

    public Supplier<ResourceLocation> texture;
    public Supplier<Rect2i> srcRect;
    public Supplier<Pair<Integer,Integer>> atlasSize;
    public Supplier<Pair<Integer,Integer>> pos;

    public static class BackgroundInteractor implements ViewInteractor<BackgroundViewDSL> {

        private ArrayList<BackgroundViewDSL> views = new ArrayList<>();

        @Override
        public void clear() {
            views.clear();
        }

        @Override
        public void addView(BackgroundViewDSL dsl) {
            views.add(dsl);
        }

        public <S extends DecentScreen<S,?>> void renderViews(S screen, PoseStack stack, int cursorX, int cursorY) {
            screen.fillGradient(stack, 0, 0, screen.width, screen.height, -1072689136, -804253680);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.BackgroundDrawnEvent(screen, stack));
            for (var view:views) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, view.texture.get());
                var rect = view.srcRect.get();
                var pos = view.pos.get();
                var atlas = view.atlasSize.get();
                screen.blit(stack, pos.getA(), pos.getB(), screen.getBlitOffset(), rect.getX(), rect.getY(),rect.getWidth(), rect.getHeight(),atlas.getA(),atlas.getB());
            }
        }

        @Override
        public <S extends DecentScreen<S,?>> void click(S screen, int cursorX, int cursorY, int key) {}

    }
}
