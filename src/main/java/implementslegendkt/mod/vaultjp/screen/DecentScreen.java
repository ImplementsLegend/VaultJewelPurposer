package implementslegendkt.mod.vaultjp.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import implementslegendkt.mod.vaultjp.screen.view.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DecentScreen<SELF extends DecentScreen<SELF,M>,M extends AbstractContainerMenu> extends Screen implements MenuAccess<M> {
    public M menu;
    private ItemStack draggingItem = ItemStack.EMPTY;

    private HashMap<Class<? extends ViewInteractor>,ViewInteractor> interactors = new HashMap();

    private List<Composition<SELF>> compositions;

    protected DecentScreen(M menu, Component p_96550_) {
        super(p_96550_);
        this.menu=menu;

    }

    protected List<Composition<SELF>> createCompositions(){
        return new ArrayList<>();
    }


    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();


        for (var interactorEntry:interactors.entrySet()) {
            var interactor = interactorEntry.getValue();
            interactor.clear();
        }

        var midX = width/2;
        var midY = height/2;

        for(var c:(compositions=createCompositions())){
            c.compose((SELF) this,midX,midY);
        }
    }

    @Override
    public void tick() {
        super.tick();
        for (var c: compositions){
            c.tick((SELF) this);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends ViewInteractor> T getOrCreateInteractor(Class<T> clazz, Function<? super Class<? extends ViewInteractor>,T> constructor){
        return (T) interactors.computeIfAbsent(clazz,constructor);
    }

    public ItemRenderer getItemRenderer(){
        return itemRenderer;
    }

    protected void slider(){

    }
    public void viewSlot(Consumer<SlotViewDSL> slotView){
        var dsl = new SlotViewDSL();
        slotView.accept(dsl);
        var interactor = getOrCreateInteractor(SlotViewDSL.SlotInteractor.class,(unused)-> new SlotViewDSL.SlotInteractor());
        interactor.addView(dsl);

    }
    public void button(Consumer<ButtonViewDSL> slotView){
        var dsl = new ButtonViewDSL();
        slotView.accept(dsl);
        var interactor = getOrCreateInteractor(ButtonViewDSL.ButtonInteractor.class,(unused)-> new ButtonViewDSL.ButtonInteractor());
        interactor.addView(dsl);
    }
    public void intBox(Consumer<IntBoxViewDSL> slotView){
        var dsl = new IntBoxViewDSL();
        slotView.accept(dsl);
        var interactor = getOrCreateInteractor(IntBoxViewDSL.IntBoxInteractor.class,(unused)-> new IntBoxViewDSL.IntBoxInteractor());
        interactor.addView(dsl);
    }
    public void text(Consumer<TextViewDSL> slotView){
        var dsl = new TextViewDSL();
        slotView.accept(dsl);
        var interactor = getOrCreateInteractor(TextViewDSL.TextInteractor.class,(unused)-> new TextViewDSL.TextInteractor());
        interactor.addView(dsl);
    }

    public void background(Consumer<BackgroundViewDSL> slotView){
        var dsl = new BackgroundViewDSL();
        slotView.accept(dsl);
        var interactor = getOrCreateInteractor(BackgroundViewDSL.BackgroundInteractor.class,(unused)-> new BackgroundViewDSL.BackgroundInteractor());
        interactor.addView(dsl);
    }

    @Override
    public void render(PoseStack p_96562_, int x, int y, float p_96565_) {
        var background = interactors.get(BackgroundViewDSL.BackgroundInteractor.class);
        background.renderViews(this,p_96562_,x,y);
        for (var interactorEntry:interactors.entrySet()) {
            var interactor = interactorEntry.getValue();
            if (interactor instanceof BackgroundViewDSL.BackgroundInteractor) continue;
            interactor.renderViews(this,p_96562_,x,y);

        }
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        for (var interactorEntry:interactors.entrySet()) {
            var interactor = interactorEntry.getValue();
            if (interactor instanceof BackgroundViewDSL.BackgroundInteractor) continue;
            interactor.type(this,p_96552_,p_96554_);

        }

        return super.keyPressed(p_96552_, p_96553_, p_96554_);
    }

    @Override
    public void fillGradient(PoseStack p_93180_, int p_93181_, int p_93182_, int p_93183_, int p_93184_, int p_93185_, int p_93186_) {
        super.fillGradient(p_93180_, p_93181_, p_93182_, p_93183_, p_93184_, p_93185_, p_93186_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int buttons) {

        for (var interactorEntry:interactors.entrySet()) {
            var interactor = interactorEntry.getValue();
            if (interactor instanceof BackgroundViewDSL.BackgroundInteractor) continue;
            interactor.click(this,(int)x,(int)y,buttons);

        }
        var background = interactors.get(BackgroundViewDSL.BackgroundInteractor.class);
        background.click(this,(int)x,(int)y,buttons);


        return super.mouseClicked(x, y, buttons);
    }

    @Override
    public M getMenu() {
        return menu;
    }

    public Font getFont() {
        return font;
    }

    public ItemStack getDraggingItem() {
        return draggingItem;
    }
}
