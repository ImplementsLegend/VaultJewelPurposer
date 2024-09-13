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
            else values.sumOf { it.getValue(VaultGearData.read(jewel)) } / size
    }

    fun isBad(jewel: ItemStack) = getJewelUsefulness(jewel) < disposeThreshold

    companion object {
        private val jewelAttributes: List<JewelAttribute> = Arrays.asList(*JewelAttribute.values())

        fun readNBT(nbt: CompoundTag) =
            JewelPurpose(
                (nbt["usefulnesses"] as? ListTag)?.filterIsInstance<CompoundTag>()?.map {
                    AttributeUsefulness(
                        jewelAttributes[it.getInt("attr")],
                        it.getDouble("mul")
                    )
                }?.toMutableList() ?: arrayListOf(),
                nbt.getDouble("trash"),
                nbt.getBoolean("div"),
                nbt.getString("name")
            )

        fun writeNBT(purpose: JewelPurpose) = CompoundTag().apply {
            val list = ListTag()

            for ((attribute1, multiplier) in purpose.values) {
                list.add(CompoundTag().apply {

                    val attribute = jewelAttributes.indexOf(attribute1)

                    putInt("attr", attribute)
                    putDouble("mul", multiplier)
                })
            }
            put("usefulnesses", list)
            putBoolean("div", purpose.divideBySize)
            putDouble("trash", purpose.disposeThreshold)
            putString("name", purpose.name)
        }
    }
}
