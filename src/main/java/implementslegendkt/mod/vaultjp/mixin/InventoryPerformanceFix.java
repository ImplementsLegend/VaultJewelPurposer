package implementslegendkt.mod.vaultjp.mixin;

import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.container.oversized.OverSizedItemStack;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverSizedInventory.class)
public abstract class InventoryPerformanceFix {
    @Shadow(remap = false) @Final private NonNullList<OverSizedItemStack> contents;

    @Inject(method = "isEmpty",at = @At("HEAD"), cancellable = true)
    private void fixPerformance(CallbackInfoReturnable<Boolean> cir){

        cir.setReturnValue(true);
        for (var cnt :
                contents) {
            if(!cnt.overSizedStack().isEmpty()){
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
