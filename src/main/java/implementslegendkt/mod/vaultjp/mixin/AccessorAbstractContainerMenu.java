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

    @Accessor List<DataSlot> getDataSlots();

    @Invoker void callSynchronizeCarriedToRemote();

    @Invoker void callTriggerSlotListeners(int i, ItemStack stack, Supplier<ItemStack> stackSupplier);
    @Invoker void callSynchronizeSlotToRemote(int i, ItemStack stack, Supplier<ItemStack> stackSupplier);
    @Invoker void callUpdateDataSlotListeners(int j, int k);
    @Invoker void callSynchronizeDataSlotToRemote(int j, int k);
}
