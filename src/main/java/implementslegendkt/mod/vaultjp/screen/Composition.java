package implementslegendkt.mod.vaultjp.screen;

public interface Composition<S extends DecentScreen<S,?>> {

    void compose(S screen, int midX, int midY);

    default void tick(S screen){};
}
