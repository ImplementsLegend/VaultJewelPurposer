package implementslegendkt.mod.vaultjp

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger
import iskallia.vault.gear.data.VaultGearData

@JvmRecord
data class AttributeUsefulness( val attribute: JewelAttribute,  val multiplier: Double) {
    fun getValue(item: VaultGearData) =
        item
            .allAttributes
            .toList()
            .filter { it.attribute==attribute.attribute }
            .sumOf { (it.value as Number).toDouble()*multiplier }
}
