package implementslegendkt.mod.screenlegends

interface Composition<S : DecentScreen<*, *>> {
    fun S.compose(midX: Int, midY: Int)

    fun tick(screen: S) {}
}
