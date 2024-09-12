package implementslegendkt.mod.screenlegends

import com.mojang.blaze3d.vertex.PoseStack

interface ViewInteractor<T : View?> {
    fun clear()
    fun addView(dsl: T)

    fun <S : DecentScreen<*, *>> renderViews(screen: S, stack: PoseStack?, cursorX: Int, cursorY: Int)

    fun <S : DecentScreen<*, *>> click(screen: S, cursorX: Int, cursorY: Int, key: Int)
    fun <S : DecentScreen<*, *>> type(screen: S, glfwKey: Int, modifiers: Int) {}
}
