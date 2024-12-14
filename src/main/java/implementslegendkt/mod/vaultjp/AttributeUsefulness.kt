package implementslegendkt.mod.vaultjp

import iskallia.vault.gear.data.VaultGearData

@JvmRecord
data class AttributeUsefulness( val attribute: JewelAttribute,  val multiplier: Double) {
    fun evaluate(item: VaultGearData) =
        item
            .allAttributes
            .toList()
            .filter { it.attribute==attribute.attribute }
            .sumOf { (it.value as Number).toDouble()*multiplier }
}
