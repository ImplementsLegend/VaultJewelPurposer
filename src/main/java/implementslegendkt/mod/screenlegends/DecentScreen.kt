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

    private val compositions by lazy { createCompositions() }

    protected open fun createCompositions(): List<Composition<SELF>> = ArrayList()

    override fun isPauseScreen(): Boolean = false

    override fun init() {
        super.init()

        forEachInteractor { it!!.clear() }

        val midX = width / 2
        val midY = height / 2

        compositions.forEach { with(it) { (this@DecentScreen as SELF).compose(midX, midY)} }
    }

    override fun tick() {
        super.tick()
        compositions.forEach { it.tick(this as SELF) }
    }

    private inline fun <reified T : ViewInteractor<*>?> getOrCreateInteractor(noinline constructor: (Class<out ViewInteractor<*>?>?)->T?): T? = interactors.computeIfAbsent(T::class.java, constructor) as T?

    var itemRenderer: ItemRenderer by ::itemRenderer

    private inline fun <reified T:ViewInteractor<K>?,reified K:View> viewToInteractor(noinline newInteractor: (Class<out ViewInteractor<*>?>?)->T?,view:K,applyFnc:K.()->Unit) = getOrCreateInteractor (newInteractor)?.addView(view.apply(applyFnc))

    fun viewSlot(slotView: SlotViewDSL.() -> Unit) = viewToInteractor({SlotInteractor()},SlotViewDSL(),slotView)
    fun button(slotView: ButtonViewDSL.() -> Unit) = viewToInteractor({ButtonInteractor()},ButtonViewDSL(),slotView)
    fun intBox(slotView: IntBoxViewDSL.()->Unit)   = viewToInteractor({IntBoxInteractor()},IntBoxViewDSL(),slotView)
    fun text(slotView: TextViewDSL.()->Unit)       = viewToInteractor({TextInteractor()},TextViewDSL(),slotView)
    fun background(slotView: BackgroundViewDSL.() -> Unit) =viewToInteractor({BackgroundInteractor()},BackgroundViewDSL(),slotView)

    override fun render(p_96562_: PoseStack, x: Int, y: Int, p_96565_: Float) {
        interactors[BackgroundInteractor::class.java]?.renderViews(this,p_96562_,x,y)
        forEachInteractorNotBackgraound { it.renderViews(this, p_96562_, x, y) }
    }

    override fun keyPressed(p_96552_: Int, p_96553_: Int, p_96554_: Int): Boolean = super.keyPressed(p_96552_, p_96553_, p_96554_).also{
        forEachInteractorNotBackgraound {  it.type(this,p_96552_,p_96554_) }
    }

    public override fun fillGradient(pose: PoseStack, p_93181_: Int, p_93182_: Int, p_93183_: Int, p_93184_: Int, p_93185_: Int, p_93186_: Int) =
        super.fillGradient(pose, p_93181_, p_93182_, p_93183_, p_93184_, p_93185_, p_93186_)

    override fun mouseClicked(x: Double, y: Double, buttons: Int) = super.mouseClicked(x,y,buttons).also {
        forEachInteractorNotBackgraound {  it.click(this, x.toInt(), y.toInt(), buttons) }
        interactors[BackgroundInteractor::class.java]!!.click(this, x.toInt(), y.toInt(), buttons)
    }
    private inline fun forEachInteractor(fnc:(ViewInteractor<*>?)->Unit) = interactors.forEach{(_, interactor) ->fnc(interactor)}
    private inline fun forEachInteractorNotBackgraound(fnc:(ViewInteractor<*>)->Unit) = forEachInteractor { if(it !is BackgroundInteractor?) fnc(it as ViewInteractor<*>) }

    override fun getMenu(): M = menu

    var font: Font by ::font
}
