package implementslegendkt.mod.vaultjp.mixin

import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.DataSlot
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker
import java.util.function.Supplier

@Mixin(AbstractContainerMenu::class)
interface AccessorAbstractContainerMenu {
    @get:Accessor("dataSlots")
    val dataSlots: List<DataSlot?>?

    @Invoker("synchronizeCarriedToRemote")
    fun callSynchronizeCarriedToRemote()

    @Invoker("triggerSlotListeners")
    fun callTriggerSlotListeners(i: Int, itemstack: ItemStack?, supplier: Supplier<ItemStack?>?)

    @Invoker("synchronizeSlotToRemote")
    fun callSynchronizeSlotToRemote(i: Int, itemstack: ItemStack?, supplier: Supplier<ItemStack?>?)

    @Invoker("updateDataSlotListeners")
    fun callUpdateDataSlotListeners(j: Int, k: Int)

    @Invoker("synchronizeDataSlotToRemote")
    fun callSynchronizeDataSlotToRemote(j: Int, k: Int)
}
