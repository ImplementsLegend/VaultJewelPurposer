package implementslegendkt.mod.screenlegends

import com.mojang.blaze3d.vertex.PoseStack
import implementslegendkt.mod.screenlegends.view.*
import implementslegendkt.mod.screenlegends.view.BackgroundViewDSL.BackgroundInteractor
import implementslegendkt.mod.screenlegends.view.ButtonViewDSL.ButtonInteractor
import implementslegendkt.mod.screenlegends.view.IntBoxViewDSL.IntBoxInteractor
import implementslegendkt.mod.screenlegends.view.SlotViewDSL.SlotInteractor
import implementslegendkt.mod.screenlegends.view.TextViewDSL.TextInteractor
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.MenuAccess
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

open class DecentScreen<SELF : DecentScreen<SELF, M>, M : AbstractContainerMenu> protected constructor(
    @JvmField var menu: M,
    p_96550_: Component?
) : Screen(p_96550_), MenuAccess<M> {

    val draggingItem: ItemStack = ItemStack.EMPTY

    private val interactors = HashMap<Class<out ViewInteractor<*>?>?, ViewInteractor<*>?>()

    private val compositions by lazy {
        createCompositions()
    }

    protected open fun createCompositions(): List<Composition<SELF>> {
        return ArrayList()
    }


    override fun isPauseScreen(): Boolean {
        return false
    }

    override fun init() {
        super.init()


        for ((_, interactor) in interactors) {
            interactor!!.clear()
        }

        val midX = width / 2
        val midY = height / 2

        compositions.forEach { it.compose(this as SELF,midX, midY) }
    }

    override fun tick() {
        super.tick()
        compositions.forEach { it.tick(this as SELF) }
    }

    private inline fun <reified T : ViewInteractor<*>?> getOrCreateInteractor(
        noinline constructor: (Class<out ViewInteractor<*>?>?)->T?
    ): T? {
        return interactors.computeIfAbsent(T::class.java, constructor) as T?
    }

    var itemRenderer: ItemRenderer
        get() = itemRenderer
        set(itemRenderer) {
            super.itemRenderer = itemRenderer
        }

    protected fun slider() {
    }

    fun viewSlot(slotView: SlotViewDSL.() -> Unit) {
        getOrCreateInteractor { SlotInteractor() }?.addView(SlotViewDSL().apply(slotView))
    }

    fun button(slotView: ButtonViewDSL.() -> Unit) {
        getOrCreateInteractor { ButtonInteractor() }?.addView(ButtonViewDSL().apply(slotView))
    }

    fun intBox(slotView: IntBoxViewDSL.()->Unit) {
        getOrCreateInteractor { IntBoxInteractor() }?.addView(IntBoxViewDSL().apply(slotView))
    }

    fun text(slotView: TextViewDSL.()->Unit) {
        getOrCreateInteractor { TextInteractor() }?.addView(TextViewDSL().apply(slotView))
    }

    fun background(slotView: BackgroundViewDSL.() -> Unit) {
        getOrCreateInteractor { BackgroundInteractor() }?.addView(BackgroundViewDSL().apply(slotView))
    }

    override fun render(p_96562_: PoseStack, x: Int, y: Int, p_96565_: Float) {
        interactors[BackgroundInteractor::class.java]?.renderViews(this,p_96562_,x,y)
        interactors.forEach {
            (_,interactor)-> interactor?.takeUnless { it is BackgroundInteractor }?.renderViews(this, p_96562_, x, y)
        }
    }

    override fun keyPressed(p_96552_: Int, p_96553_: Int, p_96554_: Int): Boolean {
        interactors.forEach {
            (_,interactor)->interactor?.takeUnless { it is BackgroundInteractor }?.type(this,p_96552_,p_96554_)
        }
        return super.keyPressed(p_96552_, p_96553_, p_96554_)
    }

    public override fun fillGradient(
        p_93180_: PoseStack,
        p_93181_: Int,
        p_93182_: Int,
        p_93183_: Int,
        p_93184_: Int,
        p_93185_: Int,
        p_93186_: Int
    ) {
        super.fillGradient(p_93180_, p_93181_, p_93182_, p_93183_, p_93184_, p_93185_, p_93186_)
    }

    override fun mouseClicked(x: Double, y: Double, buttons: Int): Boolean {
        interactors.forEach { (_, interactor) ->
            interactor?.takeUnless { it is BackgroundInteractor }?.click(this, x.toInt(), y.toInt(), buttons)
        }
        interactors[BackgroundInteractor::class.java]!!.click(this, x.toInt(), y.toInt(), buttons)


        return super.mouseClicked(x, y, buttons)
    }

    override fun getMenu(): M {
        return menu
    }

    var font: Font
        get() = super.font
        set(font) {
            super.font = font
        }
}
