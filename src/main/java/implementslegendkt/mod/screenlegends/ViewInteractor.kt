package implementslegendkt.mod.screenlegends

import com.mojang.blaze3d.vertex.PoseStack

interface ViewInteractor<T : View> {
    fun clear()
    fun addView(dsl: T)
    val views:Iterable<T>

    fun <S : DecentScreen<*, *>> renderViews(screen: S, stack: PoseStack?, cursorX: Int, cursorY: Int)

    fun <S : DecentScreen<*, *>> click(screen: S, cursorX: Int, cursorY: Int, key: Int)
    fun <S : DecentScreen<*, *>> type(screen: S, glfwKey: Int, modifiers: Int) {}
}

inline fun <T : View> ViewInteractor<T>.forEnabledViews(fnc:(T)->Unit){
    for(view in views){
        if(!view.enabled())continue
        fnc(view)
    }
}

inline fun <T : View> ViewInteractor<T>.forEnabledViewsIndexed(fnc:(index:Int,T)->Unit){
    views.forEachIndexed { index, view ->
        if(!view.enabled())return
        fnc(index,view)
    }
}