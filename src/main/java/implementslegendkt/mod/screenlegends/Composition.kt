package implementslegendkt.mod.screenlegends

interface Composition<S : DecentScreen<*, *>> {
    fun compose(screen: S, midX: Int, midY: Int)

    fun tick(screen: S) {}
}
