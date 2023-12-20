package implementslegendkt.mod.vaultjp;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;

public record AttributeUsefulness(JewelAttribute attribute, double multiplier) {
    double getValue(VaultGearData item){
        return item.get(attribute.attribute, VaultGearAttributeTypeMerger.of(()->0.0,(a,b)->a+b.doubleValue()*multiplier));

    }
}
