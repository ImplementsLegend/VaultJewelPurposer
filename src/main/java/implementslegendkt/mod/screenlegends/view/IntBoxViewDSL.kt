package implementslegendkt.mod.screenlegends.view

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import implementslegendkt.mod.screenlegends.*
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.Rect2i
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW
import java.util.*
import java.util.List
import java.util.function.Function
import kotlin.math.log10
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun defaultTextFunction(value:Int,cursorPosition:Int):Component{
    if (cursorPosition < 0) return TextComponent(value.toString())
    //this just underlines selected digit
    val str = (if (value < 0) value.toString().substring(1) else value.toString()).padStart(cursorPosition+1,'0')
    val tailStart = str.length - cursorPosition
    val tail = if (tailStart < str.length) str.substring(tailStart) else ""
    val selected = str.substring(str.length - 1 - cursorPosition, str.length - cursorPosition)
    val headEnd = str.length - 1 - cursorPosition
    val head =  /*headEnd<0?"":*/(if (value < 0) "-" else "") + str.substring(0, headEnd)
    return TextComponent(head).append(TextComponent(selected).withStyle(ChatFormatting.UNDERLINE))
        .append(TextComponent(tail))

}

class IntBoxViewDSL : View {
    var texture: ()->ResourceLocation? = { null }
    var srcRect: ()->Rect2i? = { null }
    var atlasSize: ()->Pair<Int, Int>? = { null }
    var pos: ()->Pair<Int, Int> = { 0 to 0 }
    var valueGetter: ()->Int = { 0 }
    var valueSetter: (Int)->Unit = { }
    var valueRange: ()->IntRange = { -1999999999..1999999999 }
    var textPos: (Int) -> Pair<Int, Int> = { width: Int? -> Pair(0, 0) }
    var text: (Int, Int) -> Component = ::defaultTextFunction

    var shouldHighlight: Function<Boolean, Boolean> = Function { hovering: Boolean -> hovering }

    override var enabled: () -> Boolean = {true}

    class IntBoxInteractor : ViewInteractor<IntBoxViewDSL> {
        private var selected = 0
        private var cursor = 0
        override fun clear() {
            views.clear()
        }

        override val views = ArrayList<IntBoxViewDSL>()
        override fun addView(dsl: IntBoxViewDSL) {
            views.add(dsl)
        }

        override fun <S : DecentScreen<*, *>> renderViews(screen: S, stack: PoseStack?, cursorX: Int, cursorY: Int) {
            forEnabledViewsIndexed {
                i,view ->
                run drawImage@{
                    RenderSystem.setShader { GameRenderer.getPositionTexShader() }
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
                    view.texture()?.let { RenderSystem.setShaderTexture(0, it) }
                    val rect = view.srcRect()?:return@drawImage
                    val position = view.pos()
                    val atlas = view.atlasSize()?:return@drawImage
                    RenderSystem.enableDepthTest()
                    GuiComponent.blit(
                        stack,
                        position.first,
                        position.second,
                        screen!!.getBlitOffset(),
                        rect.x.toFloat(),
                        rect.y.toFloat(),
                        rect.width,
                        rect.height,
                        atlas.first,
                        atlas.second
                    )

                    val hoveringX = cursorX > position.first && cursorX < position.first + rect.width
                    val hoveringY = cursorY > position.second && cursorY < position.second + rect.height
                    if (view.shouldHighlight.apply(hoveringX && hoveringY)) {
                        //RenderSystem.disableDepthTest()
                        GuiComponent.fill(
                            stack,
                            position.first,
                            position.second,
                            position.first + rect.width,
                            position.second + rect.height,
                            0x40ffffff
                        )
                        //RenderSystem.enableDepthTest()
                    }
                }
                run drawText@{
                    val font = screen.font
                    val text = view.text(view.valueGetter(), if (i == selected) cursor else -1)
                    val width = font.width(text)
                    val position = view.textPos(width)
                    val height = font.lineHeight
                    font.draw(stack, text, position.first.toFloat(), position.second.toFloat(), 0xffffff)
                    val hover = text.style.hoverEvent
                    if (hover != null) {
                        val hoveringX = cursorX > position.first && cursorX < position.first + width
                        val hoveringY = cursorY > position.second && cursorY < position.second + height
                        if (hoveringX && hoveringY) {
                            val action = hover.action
                            //RenderSystem.disableDepthTest()
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
                            //RenderSystem.enableDepthTest()
                        }
                    }
                }
            }
        }

        override fun <S : DecentScreen<*, *>> click(screen: S, cursorX: Int, cursorY: Int, key: Int) {
            forEnabledViewsIndexed {
                i,view->
                val position = view.pos()
                val rect = view.srcRect()
                val width = rect?.width?:return@forEnabledViewsIndexed
                val height = rect.height

                val hoveringX = cursorX > position.first && cursorX < position.first + width
                val hoveringY = cursorY > position.second && cursorY < position.second + height
                if (hoveringX && hoveringY) {
                    selected = i
                }
            }
        }

        override fun <S : DecentScreen<*, *>> type(screen: S, glfwKey: Int, modifiers: Int) {
            val view = views[selected]
            if (glfwKey == GLFW.GLFW_KEY_TAB) {
                selected = selected + (if (((modifiers and 2) == 0)) +1 else -1)
                while (selected < 0) selected += views.size
                while (selected >= views.size) selected -= views.size
            }

            var value by object :ReadWriteProperty<Any?,Int>{
                override fun getValue(thisRef: Any?, property: KProperty<*>): Int = view.valueGetter()

                override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int)  = view.valueSetter(value)
            }
            val range = view.valueRange()
            //todo view.onTyped

            if (glfwKey == GLFW.GLFW_KEY_KP_ADD || glfwKey == GLFW.GLFW_KEY_UP) {
                val newValue = value + cursorExponential
                value=newValue.takeIf { it<= range.last }?:range.last
            }
            if (glfwKey == GLFW.GLFW_KEY_LEFT) {
                cursor = Integer.min(
                    cursor + 1,
                    log10((if (value < 0) -range.first else range.last).toDouble())
                        .toInt()
                )
            }
            if (glfwKey == GLFW.GLFW_KEY_RIGHT) {
                cursor = Integer.max(cursor - 1, 0)
            }
            if (glfwKey == GLFW.GLFW_KEY_KP_SUBTRACT || glfwKey == GLFW.GLFW_KEY_DOWN) {
                val newValue = value - cursorExponential
                value=newValue.takeIf { it>= range.first }?:range.last
            }
            if (glfwKey >= GLFW.GLFW_KEY_0 && glfwKey <= GLFW.GLFW_KEY_9) {
                val digit = glfwKey - GLFW.GLFW_KEY_0
                val currentDigit = (value / cursorExponential) % 10
                val newValue = value - (currentDigit + (if ((value < 0)) digit else -digit)) * this.cursorExponential
                value=newValue.takeIf { it<= range.last }?:range.last
            }
            if (glfwKey >= GLFW.GLFW_KEY_KP_0 && glfwKey <= GLFW.GLFW_KEY_KP_9) {
                val digit = glfwKey - GLFW.GLFW_KEY_KP_0
                val currentDigit = (value / cursorExponential) % 10
                val newValue = value - (currentDigit + (if ((value < 0)) digit else -digit)) * this.cursorExponential
                value=newValue.takeIf { it<= range.last }?:range.last
            }
        }

        private val cursorExponential: Int
            get() {
                var result = 1
                for (i in 0 until cursor) {
                    result *= 10
                }
                return result
            }
    }
}
