package implementslegendkt.mod.vaultjp

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger
import iskallia.vault.gear.data.VaultGearData
import iskallia.vault.init.ModGearAttributes
import iskallia.vault.item.tool.JewelItem
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack
import java.util.*

@JvmRecord
data class JewelPurpose(
    val values: MutableList<AttributeUsefulness>,
    val disposeThreshold: Double,
    val divideBySize: Boolean,
    val name: String
) {
    fun getJewelUsefulness(jewel: ItemStack): Double {
        if (jewel.item !is JewelItem) return Double.NEGATIVE_INFINITY

        val size = VaultGearData.read(jewel).get(
                ModGearAttributes.JEWEL_SIZE,
                VaultGearData.Type.ALL,
                VaultGearAttributeTypeMerger.firstNonNull()
            )
        return if (size == null || size <= 0) Double.POSITIVE_INFINITY
            else values.sumOf { usefulness -> usefulness.evaluate(VaultGearData.read(jewel)) } / size
    }

    fun isBad(jewel: ItemStack) = getJewelUsefulness(jewel) < disposeThreshold

    companion object {
        private val jewelAttributes: List<JewelAttribute> = Arrays.asList(*JewelAttribute.values())

        fun readNBT(nbt: CompoundTag) = nbt.run {
            JewelPurpose(
                (this["usefulnesses"] as? ListTag)?.filterIsInstance<CompoundTag>()?.map { usefulnessEntry ->
                    AttributeUsefulness(
                        jewelAttributes[usefulnessEntry.getInt("attr")],
                        usefulnessEntry.getDouble("mul")
                    )
                }?.toMutableList() ?: arrayListOf(),
                getDouble("trash"),
                getBoolean("div"),
                getString("name")
            )
        }

        fun writeNBT(purpose: JewelPurpose) = CompoundTag().apply {
            put("usefulnesses", ListTag().apply {

                purpose.values.forEach { (attribute1, multiplier) ->
                    add(CompoundTag().apply {

                        val attribute = jewelAttributes.indexOf(attribute1)

                        putInt("attr", attribute)
                        putDouble("mul", multiplier)
                    })
                }
            })
            putBoolean("div", purpose.divideBySize)
            putDouble("trash", purpose.disposeThreshold)
            putString("name", purpose.name)
        }
    }
}
