package implementslegendkt.mod.vaultjp

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger
import iskallia.vault.gear.data.VaultGearData
import iskallia.vault.init.ModGearAttributes
import iskallia.vault.item.tool.JewelItem
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
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
        val data = VaultGearData.read(jewel)

        val acc = values.sumOf { it.getValue(data) }
        if (divideBySize) {
            val size = data.get(
                ModGearAttributes.JEWEL_SIZE,
                VaultGearData.Type.ALL,
                VaultGearAttributeTypeMerger.firstNonNull()
            )
            if (size == null || size <= 0) return Double.POSITIVE_INFINITY
            return acc / size
        } else return acc
    }

    fun isBad(jewel: ItemStack): Boolean {
        return getJewelUsefulness(jewel) < disposeThreshold
    }

    companion object {
        private val jewelAttributes: List<JewelAttribute> = Arrays.asList(*JewelAttribute.values())

        fun readNBT(nbt: CompoundTag): JewelPurpose {

            val values = ArrayList<AttributeUsefulness>()
            for (tag in (nbt["usefulnesses"] as? ListTag)?: emptyList<Tag>()) {
                if (tag is CompoundTag) values.add(
                    AttributeUsefulness(
                        jewelAttributes[tag.getInt("attr")],
                        tag.getDouble("mul")
                    )
                )
            }
            return JewelPurpose(values, nbt.getDouble("trash"), nbt.getBoolean("div"), nbt.getString("name"))
        }

        fun writeNBT(purpose: JewelPurpose): CompoundTag {
            return CompoundTag().apply {
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
}
