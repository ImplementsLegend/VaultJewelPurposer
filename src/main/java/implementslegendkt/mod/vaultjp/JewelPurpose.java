package implementslegendkt.mod.vaultjp;

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record JewelPurpose(List<AttributeUsefulness> values, double disposeThreshold, boolean divideBySize,String name) {

    private static final List<JewelAttribute> jewelAttributes = Arrays.asList(JewelAttribute.values());

    public double getJewelUsefulness(ItemStack jewel){
        if(!(jewel.getItem() instanceof JewelItem))return -1;
        var data = VaultGearData.read(jewel);

        var acc = 0.0;
        for(var att:values){
            acc+=att.getValue(data);
        }
        if(divideBySize) {
            var size = data.get(ModGearAttributes.JEWEL_SIZE, VaultGearData.Type.ALL,  VaultGearAttributeTypeMerger.firstNonNull());
            if(size==null || size<=0) return Double.POSITIVE_INFINITY;
            return acc / size;
        } else return acc;
    }

    public static JewelPurpose readNBT(CompoundTag nbt){
        var values =new ArrayList<AttributeUsefulness>();
        if(nbt.get("usefulnesses") instanceof ListTag listTag) for (var tag:listTag) {
            if(tag instanceof CompoundTag comtag)values.add(new AttributeUsefulness(jewelAttributes.get(comtag.getInt("attr")), comtag.getDouble("mul")));
        }
        return new JewelPurpose(values,nbt.getDouble("trash"),nbt.getBoolean("div"),nbt.getString("name"));
    }
    public static CompoundTag writeNBT(JewelPurpose purpose){
        var result = new CompoundTag();
        var list = new ListTag();

        for (var attr:purpose.values) {
            var usefulnessTag = new CompoundTag();

            var attribute = jewelAttributes.indexOf(attr.attribute());
            var multiplier = attr.multiplier();

            usefulnessTag.putInt("attr", attribute);
            usefulnessTag.putDouble("mul", multiplier);
            list.add(usefulnessTag);

        }
        result.put("usefulnesses",list);
        result.putBoolean("div", purpose.divideBySize);
        result.putDouble("trash",purpose.disposeThreshold);
        result.putString("name",purpose.name);
        return result;

    }
}
