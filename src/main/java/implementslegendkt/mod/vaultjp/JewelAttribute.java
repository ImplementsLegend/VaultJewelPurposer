package implementslegendkt.mod.vaultjp;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModGearAttributes;

public enum JewelAttribute {
    MINING_SPEED(ModGearAttributes.MINING_SPEED, "vaultjp.attrib.mining_speed"),
    DURABILITY(ModGearAttributes.DURABILITY, "vaultjp.attrib.durability"),
    TRAP_DISARMING(ModGearAttributes.TRAP_DISARMING, "vaultjp.attrib.trap_disarm"),
    REACH(ModGearAttributes.REACH, "vaultjp.attrib.reach"),
    COPIOUSLY(ModGearAttributes.COPIOUSLY, "vaultjp.attrib.copious"),
    HAMMER_SIZE(ModGearAttributes.HAMMER_SIZE, "vaultjp.attrib.hammer"),
    ITEM_QUANTITY(ModGearAttributes.ITEM_QUANTITY, "vaultjp.attrib.item_quan"),
    ITEM_RARITY(ModGearAttributes.ITEM_RARITY, "vaultjp.attrib.item_rar"),
    IMMORTALITY(ModGearAttributes.IMMORTALITY, "vaultjp.attrib.immor"),
   // JEWEL_SIZE(ModGearAttributes.JEWEL_SIZE)
   /*
    MINING_SPEED(ModGearAttributes.SOULBOUND),
    MINING_SPEED(ModGearAttributes.SMELTING),
    MINING_SPEED(ModGearAttributes.PICKING),
    MINING_SPEED(ModGearAttributes.PULVERIZING),*/
    ;

    final VaultGearAttribute<? extends Number>  attribute;
    private String translationKey;

    JewelAttribute(VaultGearAttribute<? extends Number>  attribute, String translationKey) {

        this.attribute = attribute;
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
