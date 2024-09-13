package implementslegendkt.mod.vaultjp.mixin

import iskallia.vault.container.oversized.OverSizedInventory
import iskallia.vault.container.oversized.OverSizedItemStack
import net.minecraft.core.NonNullList
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(OverSizedInventory::class)
interface AccessorOverSizedInventory {
    @get:Accessor("contents")
    val contentsOverSized: NonNullList<OverSizedItemStack?>?
}
