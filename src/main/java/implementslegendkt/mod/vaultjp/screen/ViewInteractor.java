package implementslegendkt.mod.vaultjp.screen;

import com.mojang.blaze3d.vertex.PoseStack;

public interface ViewInteractor<T extends View> {

    void clear();
    void addView(T dsl);

    <S extends DecentScreen<S,?>> void renderViews(S screen, PoseStack stack, int cursorX, int cursorY);

    <S extends DecentScreen<S,?>> void click(S screen,int cursorX, int cursorY, int key);
}
