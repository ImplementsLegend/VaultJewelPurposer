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
import net.minecraftforge.client.event.ScreenEvent.BackgroundDrawnEvent
import net.minecraftforge.common.MinecraftForge

class BackgroundViewDSL : View {
    var texture: ()->ResourceLocation? = { null }
    var srcRect: ()->Rect2i? = { null }
    var atlasSize: ()->Pair<Int, Int>? = { null }
    var pos: ()->Pair<Int, Int> = { 0 to 0}

    class BackgroundInteractor : ViewInteractor<BackgroundViewDSL> {
        private val views = ArrayList<BackgroundViewDSL>()

        override fun clear() {
            views.clear()
        }

        override fun addView(dsl: BackgroundViewDSL) {
            views.add(dsl)
        }

        override fun <S : DecentScreen<*, *>> renderViews(screen: S, stack: PoseStack?, cursorX: Int, cursorY: Int) {
            screen!!.fillGradient(stack!!, 0, 0, screen.width, screen.height, -1072689136, -804253680)
            MinecraftForge.EVENT_BUS.post(BackgroundDrawnEvent(screen, stack))
            for (view in views) {
                RenderSystem.setShader { GameRenderer.getPositionTexShader() }
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
                view.texture()?.let { RenderSystem.setShaderTexture(0, it) }
                val rect = view.srcRect()?:continue
                val pos = view.pos()
                val atlas = view.atlasSize()?:continue
                GuiComponent.blit(
                    stack,
                    pos.first,
                    pos.second,
                    screen.getBlitOffset(),
                    rect.x.toFloat(),
                    rect.y.toFloat(),
                    rect.width,
                    rect.height,
                    atlas.first,
                    atlas.second
                )
            }
        }

        override fun <S : DecentScreen<*, *>> click(screen: S, cursorX: Int, cursorY: Int, key: Int) {}
    }
}
