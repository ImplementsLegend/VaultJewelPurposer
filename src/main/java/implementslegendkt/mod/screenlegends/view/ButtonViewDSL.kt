package implementslegendkt.mod.screenlegends.view

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import implementslegendkt.mod.screenlegends.DecentScreen
import implementslegendkt.mod.screenlegends.View
import implementslegendkt.mod.screenlegends.ViewInteractor
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.Rect2i
import net.minecraft.resources.ResourceLocation
import java.util.function.Function
import java.util.function.Supplier

class ButtonViewDSL : View {
    var texture: ()->ResourceLocation? = { null }
    var srcRect: ()->Rect2i? = { null }
    var atlasSize: ()->Pair<Int, Int>? = { null }
    var pos: ()->Pair<Int, Int> = { 0 to 0 }
    var onClick: ()->Unit = {}

    var shouldHighlight: Function<Boolean, Boolean> = Function { hovering: Boolean -> hovering }

    class ButtonInteractor : ViewInteractor<ButtonViewDSL> {
        override fun clear() {
            views.clear()
        }

        private val views = ArrayList<ButtonViewDSL>()
        override fun addView(dsl: ButtonViewDSL) {
            views.add(dsl)
        }

        override fun <S : DecentScreen<*, *>> renderViews(screen: S, stack: PoseStack?, cursorX: Int, cursorY: Int) {
            for (view in views) {
                RenderSystem.setShader { GameRenderer.getPositionTexShader() }
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
                view.texture()?.let { RenderSystem.setShaderTexture(0, it) }
                val rect = view.srcRect()?:continue
                val position = view.pos()
                val atlas = view.atlasSize()?:continue
                GuiComponent.blit(
                    stack,
                    position.first,
                    position.second,
                    screen.getBlitOffset(),
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
                    RenderSystem.disableDepthTest()
                    GuiComponent.fill(
                        stack,
                        position.first,
                        position.second,
                        position.first + rect.width,
                        position.second + rect.height,
                        0x40ffffff
                    )
                    RenderSystem.enableDepthTest()
                }
            }
        }

        override fun <S : DecentScreen<*, *>> click(screen: S, cursorX: Int, cursorY: Int, key: Int) {
            for (view in views) {
                val position = view.pos()
                val srcrect = view.srcRect()?:continue
                if (cursorX > position.first && cursorX < position.first + srcrect.width && cursorY > position.second && cursorY < position.second + srcrect.height) {
                    //view.clicked=true;
                    view.onClick()
                } else {
                    //view.clicked=false;
                }
            }
        }
    }
}
