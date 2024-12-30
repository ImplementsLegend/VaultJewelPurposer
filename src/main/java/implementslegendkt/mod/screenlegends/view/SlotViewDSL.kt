package implementslegendkt.mod.screenlegends.view

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import implementslegendkt.mod.screenlegends.DecentScreen
import implementslegendkt.mod.screenlegends.View
import implementslegendkt.mod.screenlegends.ViewInteractor
import implementslegendkt.mod.screenlegends.forEnabledViews
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraftforge.client.RenderProperties
import java.util.function.Function
import java.util.function.Supplier

class SlotViewDSL : View {
    var slot: ()->Int = { -1 }
    var position: ()->Pair<Int, Int> = { 0 to 0 }
    var mapItem: (ItemStack?)-> ItemStack? = { it }
    var shouldHighlight: (Boolean) -> Boolean = { it }
    var highlightColor: (Boolean) -> Int = { -0x7f000001 }
    var zOffset: ()->Float = {0f}

    override var enabled: () -> Boolean = {true}

    class SlotInteractor : ViewInteractor<SlotViewDSL> {
        override fun clear() {
            views.clear()
        }

        override val views = ArrayList<SlotViewDSL>()
        override fun addView(dsl: SlotViewDSL) {
            views.add(dsl)
        }

        override fun <S : DecentScreen<*, *>> renderViews(screen: S, stack: PoseStack?, cursorX: Int, cursorY: Int) {

            forEnabledViews {view->
                val slot = view.slot()
                val oldItemStack =
                    if ((screen.menu!!.slots.size > slot && slot >= 0)) screen.menu!!.getSlot(slot).item else ItemStack.EMPTY
                val itemStack = view.mapItem(
                        oldItemStack
                    )
                val position = view.position()
                renderFloatingItem(screen, itemStack, position.first, position.second, null, view.zOffset())

                val hoveringX = cursorX > position.first && cursorX < position.first + 16
                val hoveringY = cursorY > position.second && cursorY < position.second + 16
                if (view.shouldHighlight(hoveringX && hoveringY)) {
                    //RenderSystem.disableDepthTest()
                    GuiComponent.fill(stack, position.first, position.second, position.first + 16, position.second + 16, view.highlightColor(hoveringX && hoveringY))
                    //RenderSystem.enableDepthTest()
                }
            }
            forEnabledViews {view->
                val slot = view.slot()
                val oldItemStack =
                    if ((screen.menu!!.slots.size > slot && slot >= 0)) screen.menu!!.getSlot(slot).item else ItemStack.EMPTY
                val itemStack = view.mapItem(
                        oldItemStack
                    )
                val position = view.position()

                val hoveringX = cursorX > position.first && cursorX < position.first + 16
                val hoveringY = cursorY > position.second && cursorY < position.second + 16
                if (hoveringX && hoveringY && (screen.menu!!.carried.isEmpty && (itemStack?.isEmpty==false))) {
                    //RenderSystem.disableDepthTest()
                    screen.renderTooltip(
                        stack,
                        screen.getTooltipFromItem(itemStack),
                        itemStack.tooltipImage,
                        cursorX,
                        cursorY
                    )
                    //RenderSystem.enableDepthTest()
                }
            }
        }

        override fun <S : DecentScreen<*, *>> click(screen: S, cursorX: Int, cursorY: Int, key: Int) {
            forEnabledViews {view->
                val slot = view.slot()
                //var itemStack = view.mapItem.apply((screen.menu.slots.size() > slot && slot>0)?screen.menu.getSlot(slot).getItem():ItemStack.EMPTY);
                val position = view.position()
                if (cursorX > position.first && cursorX < position.first + 16) {
                    if (cursorY > position.second && cursorY < position.second + 16) {
                        if (screen.menu!!.slots.size > slot && slot >= 0) {
                            val flag2 = slot != -999 && (InputConstants.isKeyDown(
                                Minecraft.getInstance().window.window,
                                340
                            ) || InputConstants.isKeyDown(Minecraft.getInstance().window.window, 344))
                            val clicktype = when{
                                flag2->ClickType.QUICK_MOVE
                                slot==-999->ClickType.THROW
                                else->ClickType.PICKUP
                            }
                            screen.getMinecraft().gameMode!!.handleInventoryMouseClick(
                                screen.menu!!.containerId,
                                slot,
                                key,
                                clicktype,
                                screen.getMinecraft().player
                            )
                            screen.menu!!.incrementStateId()
                        }
                    }
                }
            }
        }

        protected fun <S : DecentScreen<*, *>> renderFloatingItem(
            screen: S,
            p_97783_: ItemStack?,
            p_97784_: Int,
            p_97785_: Int,
            p_97786_: String?,
            zOffset: Float
        ) {
            RenderSystem.applyModelViewMatrix()
            screen.apply {
                setBlitOffset(0)
                itemRenderer.apply {
                    blitOffset=zOffset-200f
                    renderAndDecorateItem(p_97783_, p_97784_, p_97785_)
                    val font = RenderProperties.get(p_97783_).getFont(p_97783_)?:screen.font
                    renderGuiItemDecorations(
                        font,
                        p_97783_,
                        p_97784_,
                        p_97785_ - (if (screen.draggingItem.isEmpty) 0 else 8),
                        p_97786_
                    )
                    blitOffset = 0.0f
                }
                setBlitOffset(0)
            }
        }
    }
}
