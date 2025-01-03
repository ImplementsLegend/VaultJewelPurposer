package implementslegendkt.mod.vaultjp

import iskallia.vault.gear.attribute.VaultGearAttribute
import iskallia.vault.init.ModGearAttributes

enum class JewelAttribute(
    val attribute: VaultGearAttribute<out Number>,
    val translationKey: String,
    val normalization: Double
) {
    MINING_SPEED(ModGearAttributes.MINING_SPEED, "vaultjp.attrib.mining_speed", 1.0),
    DURABILITY(ModGearAttributes.DURABILITY, "vaultjp.attrib.durability", 100.0),
    TRAP_DISARMING(ModGearAttributes.TRAP_DISARMING, "vaultjp.attrib.trap_disarm", 0.05),
    REACH(ModGearAttributes.REACH, "vaultjp.attrib.reach", 0.1),
    COPIOUSLY(ModGearAttributes.COPIOUSLY, "vaultjp.attrib.copious", 0.1),
    HAMMER_SIZE(ModGearAttributes.HAMMER_SIZE, "vaultjp.attrib.hammer", 1.0),
    ITEM_QUANTITY(ModGearAttributes.ITEM_QUANTITY, "vaultjp.attrib.item_quan", 0.005),
    ITEM_RARITY(ModGearAttributes.ITEM_RARITY, "vaultjp.attrib.item_rar", 0.005),
    IMMORTALITY(ModGearAttributes.IMMORTALITY, "vaultjp.attrib.immor", 1.0)
}
