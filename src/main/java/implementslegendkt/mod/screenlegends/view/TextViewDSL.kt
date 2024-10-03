package implementslegendkt.mod.screenlegends.view

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import implementslegendkt.mod.screenlegends.DecentScreen
import implementslegendkt.mod.screenlegends.View
import implementslegendkt.mod.screenlegends.ViewInteractor
import implementslegendkt.mod.screenlegends.forEnabledViews
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.TextComponent
import java.util.*
import java.util.List
import java.util.function.Function
import java.util.function.Supplier
import kotlin.collections.ArrayList

class TextViewDSL : View {
    var pos: (Int)-> Pair<Int, Int> =  { width -> 0 to 0 }
    var text: ()->Component = { TextComponent("") }

    override var enabled: () -> Boolean = {true}

    class TextInteractor : ViewInteractor<TextViewDSL> {
        override val views = ArrayList<TextViewDSL>()

        override fun clear() {
            views.clear()
        }

        override fun addView(dsl: TextViewDSL) {
            views.add(dsl)
        }

        override fun <S:DecentScreen<*,*>> renderViews(screen: S, stack: PoseStack?, cursorX: Int, cursorY: Int) {
            forEnabledViews {view->
                val font = screen.font
                val text = view.text()
                val width = font.width(text)
                val position = view.pos(width)
                val height = font.lineHeight
                font.draw(stack, text, position.first.toFloat(), position.second.toFloat(), 0xffffff)
                val hover = text.style.hoverEvent
                if (hover != null) {
                    val hoveringX = cursorX > position.first && cursorX < position.first + width
                    val hoveringY = cursorY > position.second && cursorY < position.second + height
                    if (hoveringX && hoveringY) {
                        val action = hover.action
                        RenderSystem.disableDepthTest()
                        if (action == HoverEvent.Action.SHOW_TEXT) {
                            val txt = hover.getValue(HoverEvent.Action.SHOW_TEXT)
                            screen.renderTooltip(stack, List.of(txt), Optional.empty(), cursorX, cursorY)
                        } else if (action == HoverEvent.Action.SHOW_ITEM) {
                            val info = hover.getValue(HoverEvent.Action.SHOW_ITEM)
                            screen.renderTooltip(
                                stack,
                                screen.getTooltipFromItem(info!!.itemStack),
                                info.itemStack.tooltipImage,
                                cursorX,
                                cursorY
                            )
                        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
                            val info = hover.getValue(HoverEvent.Action.SHOW_ENTITY)
                            screen.renderTooltip(stack, info!!.tooltipLines, Optional.empty(), cursorX, cursorY)
                        } else { /*unknown tooltip type*/
                        }
                        RenderSystem.enableDepthTest()
                    }
                }
            }
        }


        override fun <S : DecentScreen<*, *>> click(screen: S, cursorX: Int, cursorY: Int, key: Int) {}
    }
}
