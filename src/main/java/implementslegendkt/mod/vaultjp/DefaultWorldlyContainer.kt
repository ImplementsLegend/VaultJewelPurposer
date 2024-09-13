package implementslegendkt.mod.vaultjp;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface DefaultWorldlyContainer extends WorldlyContainer {

    @Override
    default int[] getSlotsForFace(Direction p_19238_) {
        var indices = new int[getContainerSize()];
        for(var i = 0;i<indices.length;i++)indices[i]=i;
        return indices;
    }

    @Override
    default boolean canPlaceItemThroughFace(int p_19235_, ItemStack p_19236_, @Nullable Direction p_19237_) {
        return true;
    }

    @Override
    default boolean canTakeItemThroughFace(int p_19239_, ItemStack p_19240_, Direction p_19241_) {
        return true;
    }
}
