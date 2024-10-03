package implementslegendkt.mod.vaultjp.mixin

import iskallia.vault.item.JewelPouchItem
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Invoker

@Mixin(JewelPouchItem::class)
interface JewelPouchAccessor {
    @Invoker fun callGenerateJewels(stack:ItemStack, vaultLevel:Int, additionalIdentifiedJewels:Int)
}