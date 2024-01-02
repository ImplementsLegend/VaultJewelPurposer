package implementslegendkt.mod.vaultjp.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.function.Supplier;

@Mixin(AbstractContainerMenu.class)
public interface AccessorAbstractContainerMenu {
    @Accessor("dataSlots")
    List<DataSlot> getDataSlots();

    @Invoker("synchronizeCarriedToRemote")
    void callSynchronizeCarriedToRemote();

    @Invoker("triggerSlotListeners")
    void callTriggerSlotListeners(int i, ItemStack itemstack, Supplier<ItemStack> supplier);

    @Invoker("synchronizeSlotToRemote")
    void callSynchronizeSlotToRemote(int i, ItemStack itemstack, Supplier<ItemStack> supplier);

    @Invoker("updateDataSlotListeners")
    void callUpdateDataSlotListeners(int j, int k);

    @Invoker("synchronizeDataSlotToRemote")
    void callSynchronizeDataSlotToRemote(int j, int k);

}
