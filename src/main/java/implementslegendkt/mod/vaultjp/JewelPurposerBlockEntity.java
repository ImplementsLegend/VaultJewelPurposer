package implementslegendkt.mod.vaultjp;

import implementslegendkt.mod.vaultjp.network.Channel;
import implementslegendkt.mod.vaultjp.network.UpdatePurposesPacket;
import iskallia.vault.block.entity.VaultJewelApplicationStationTileEntity;
import iskallia.vault.block.entity.VaultJewelCuttingStationTileEntity;
import iskallia.vault.config.VaultJewelCuttingConfig;
import iskallia.vault.container.VaultJewelApplicationStationContainer;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.ToolGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModAttributes;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.item.tool.ToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class JewelPurposerBlockEntity extends BlockEntity implements MenuProvider {

    public static final BlockEntityType<JewelPurposerBlockEntity> TYPE = BlockEntityType.Builder.of(JewelPurposerBlockEntity::new, JewelPurposerBlock.INSTANCE).build(null);
    private final JewelPurposerInventory inventory = new JewelPurposerInventory();

    public ArrayList<JewelPurpose> purposes = new ArrayList<>();

    private LazyOptional<? extends IItemHandler> handler = LazyOptional.of(()->new InvWrapper(inventory));
    public JewelPurposerBlockEntity(BlockPos p_155229_, BlockState p_155230_) {

        super(TYPE, p_155229_, p_155230_);
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Jewel Purposer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return this.getLevel() == null ? null : new JewelPurposerContainer(p_39954_, this.getLevel(), this.getBlockPos(), p_39956_.getInventory());
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (!this.remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return this.handler.cast();

        return super.getCapability(cap);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.handler = LazyOptional.of(()->new InvWrapper(inventory));
    }


    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (var purposeTag:tag.getList("purposes",CompoundTag.TAG_COMPOUND)) {
            purposes.add(JewelPurpose.readNBT((CompoundTag) purposeTag));
        }
        this.inventory.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        var purposesTag = new ListTag();
        for (var purpose :purposes) {
            purposesTag.add(JewelPurpose.writeNBT(purpose));
        }
        tag.put("purposes",purposesTag);
        this.inventory.save(tag);
    }


    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @javax.annotation.Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public JewelPurposerInventory getInventory() {
        return this.inventory;
    }


    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this && this.inventory.stillValid(player);
    }


    public ItemStack applyJewelsMock(ItemStack tool, int[] jewels) {
        for (var jew : jewels) {
            if(jew<0 || jew>JewelPurposerContainer.JEWEL_COUNT_MAX)break;
            VaultJewelApplicationStationTileEntity.applyJewel(tool,inventory.getItem(jew));
        }
        return tool;
    }
    public void applyJewels(int[] jewels) {
        var toolIndex = inventory.getContainerSize()-1;
        var toolOld = inventory.removeItemNoUpdate(toolIndex);
        ItemStack tool;
        if(!(toolOld.getItem() instanceof ToolItem)){
            inventory.setItem(toolIndex,toolOld);
            return;
        }

        for (var jew : jewels) {
            if(jew>=0 && jew<JewelPurposerContainer.JEWEL_COUNT_MAX) {
                if (ToolGearData.read(toolOld).get(ModGearAttributes.TOOL_CAPACITY, VaultGearAttributeTypeMerger.intSum()) <10)break;
                tool=toolOld.copy();
                var jewel = inventory.removeItemNoUpdate(jew);
                VaultJewelApplicationStationTileEntity.applyJewel(tool,jewel);
                if(ToolGearData.read(tool).get(ModGearAttributes.TOOL_CAPACITY, VaultGearAttributeTypeMerger.intSum()) <0){
                    inventory.setItem(jew,jewel);
                }else {
                    toolOld=tool;
                }
            }
        }

        inventory.setItem(toolIndex,toolOld);

    }

    private int mod = 0;
    public void tick() {
        if(level.isClientSide())return;
        mod= (mod+1)%16;
        var range = ModConfigs.VAULT_JEWEL_CUTTING_CONFIG.getJewelCuttingRange();
        for (int i = mod; i < JewelPurposerContainer.JEWEL_COUNT_MAX; i+=16) {
            if (inventory.getItem(i).isEmpty()) continue;
            var item = inventory.removeItemNoUpdate(i);
            if (!(item.getItem() instanceof JewelItem)) {
                inventory.setItem(i, item);
                continue;
            }
            var tag = item.getOrCreateTag();
            var cuts = tag.getInt("freeCuts");
            if (cuts < 3) {
                tag=tag.copy();
                var data = VaultGearData.read(item);
                var jewelSize = data.getModifiers(ModGearAttributes.JEWEL_SIZE, VaultGearData.Type.ALL);
                for (var cuts_ = cuts; cuts_ < 3 && jewelSize.stream().anyMatch((it) -> it.getValue() > 10); cuts_++) {
                    for (var sizeInstance : jewelSize) {
                        sizeInstance.setValue(Integer.max(10, sizeInstance.getValue() - range.getRandom()));
                    }
                }
                tag.putInt("freeCuts", 3);
                item.setTag(tag);
                data.write(item);
            }
            inventory.setItem(i,item);
            if (cuts < 3)tryRecycleJewel(i);

        }
    }
    private void tryRecycleJewel(int slot){
        var jewel = inventory.getItem(slot);
        if(purposes.isEmpty() || slot<0 || slot>= JewelPurposerContainer.JEWEL_COUNT_MAX){
            return;
        }
        for (var purpose :
                purposes) {
            var usefulness = purpose.getJewelUsefulness(jewel);
            if (usefulness>purpose.disposeThreshold())return;
        }
        inventory.removeItemNoUpdate(slot);
        inventory.setChanged();
    }

    public void disposeBad() {
        if(purposes.isEmpty()){
            return;
        }
        for (int i = 0; i <JewelPurposerContainer.JEWEL_COUNT_MAX; i++) {
            tryRecycleJewel(i);
        }
    }

    public void syncToServer() {
        Channel.CHANNEL.sendToServer(new UpdatePurposesPacket(getBlockPos(),purposes));
    }


    public class JewelPurposerInventory extends OverSizedInventory implements DefaultWorldlyContainer {
        public JewelPurposerInventory() {super(JewelPurposerContainer.JEWEL_COUNT_MAX + 16+1, JewelPurposerBlockEntity.this);}

    }

}
