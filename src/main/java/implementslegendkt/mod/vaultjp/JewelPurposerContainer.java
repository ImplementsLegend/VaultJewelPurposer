package implementslegendkt.mod.vaultjp;

import com.google.common.base.Suppliers;
import implementslegendkt.mod.vaultjp.mixin.AccessorAbstractContainerMenu;
import implementslegendkt.mod.vaultjp.mixin.AccessorOverSizedInventory;
import iskallia.vault.container.oversized.OverSizedSlotContainer;
import iskallia.vault.container.slot.TabSlot;
import iskallia.vault.init.ModSlotIcons;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.item.tool.ToolItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;

import java.sql.DriverManager;
import java.util.function.Supplier;

public class JewelPurposerContainer extends OverSizedSlotContainer {
    public static final MenuType<JewelPurposerContainer> MENU_TYPE = IForgeMenuType.create((int windowId, Inventory inv, FriendlyByteBuf data)-> {
        Level world = inv.player.level;
        BlockPos pos = data.readBlockPos();
        return new JewelPurposerContainer(windowId, world, pos, inv);
    });
    public static final int JEWEL_COUNT_MAX = 256*96;

    JewelPurposerBlockEntity tileEntity;
    private final BlockPos tilePos;

    public JewelPurposerContainer(int windowId, Level world, BlockPos pos, Inventory playerInventory) {
        super(MENU_TYPE, windowId, playerInventory.player);
        this.tilePos = pos;
        var tile = world.getBlockEntity(this.tilePos);
        if (tile instanceof JewelPurposerBlockEntity craftingStationTileEntity) {
            this.tileEntity = craftingStationTileEntity;
            this.initSlots(playerInventory);
        } else {
            this.tileEntity = null;
        }

    }

    private void initSlots(Inventory playerInventory) {
        for(var row = 0; row < 3; ++row) {
            for(var column = 0; column < 9; ++column) {
                this.addSlot(new TabSlot(playerInventory, column + row * 9 + 9, 58 + column * 18, 108 + row * 18));
            }
        }

        for(var hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
            this.addSlot(new TabSlot(playerInventory, hotbarSlot, 58 + hotbarSlot * 18, 166));
        }

        Container invContainer = this.tileEntity.getInventory();

        for(var row = 0; row < 256; ++row) {
            for(int column = 0; column < 96; ++column) {
                final var overrideIndex = row * 96 + column;
                this.addSlot(new TabSlot(invContainer, overrideIndex , -999 + column * 18, 50 + row * 18) {
                    public boolean mayPlace(ItemStack stack) {
                        return stack.getItem() instanceof JewelItem;
                    }

                    @Override
                    public ItemStack getItem() {
                        var itemstack0 = ((AccessorOverSizedInventory)invContainer).getContentsOverSized().get(overrideIndex);
                        var itemstack = new ItemStack(itemstack0.stack().getItem(),itemstack0.amount());
                        itemstack.setTag(itemstack0.stack().getTag());
                        return itemstack;
                    }
                });
            }
        }

        for(var row = 0; row < 4; ++row) {
            for(int column = 0; column < 4; ++column) {
                /*
                * Extra slots intended for other purposes; currently unused
                * */
                this.addSlot(new TabSlot(invContainer, row * 4 + column + JewelPurposerContainer.JEWEL_COUNT_MAX, -999 + column * 18, 50 + row * 18) {
                    public boolean mayPlace(ItemStack stack) {
                        return false;//!(stack.getItem() instanceof JewelItem) && !(stack.getItem() instanceof ToolItem);
                    }
                });
            }
        }
        this.addSlot((new TabSlot(invContainer, JewelPurposerContainer.JEWEL_COUNT_MAX+16, 120, 73) {
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ToolItem;
            }
        }).setBackground(InventoryMenu.BLOCK_ATLAS, ModSlotIcons.TOOL_NO_ITEM));

    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (index >= 0 && index < 36 && this.moveOverSizedItemStackTo(slotStack, slot, 36, this.slots.size(), false)) {
                return itemstack;
            }

            if (index >= 0 && index < 27) {
                if (!this.moveOverSizedItemStackTo(slotStack, slot, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 27 && index < 36) {
                if (!this.moveOverSizedItemStackTo(slotStack, slot, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveOverSizedItemStackTo(slotStack, slot, 0, 36, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    public JewelPurposerBlockEntity getTileEntity() {
        return this.tileEntity;
    }

    public boolean stillValid(Player player) {
        return this.tileEntity != null && this.tileEntity.stillValid(this.player);
    }


    //speed up!
    @Override
    public void broadcastChanges() {
        for(int i = 0; i < this.slots.size(); ++i) {
            var itemstack = this.slots.get(i).getItem();
            Supplier<ItemStack> supplier = Suppliers.memoize(()->itemstack);
            ((AccessorAbstractContainerMenu)this).callTriggerSlotListeners(i, itemstack, supplier);
            ((AccessorAbstractContainerMenu)this).callSynchronizeSlotToRemote(i, itemstack, supplier);
        }
        ((AccessorAbstractContainerMenu)this).callSynchronizeCarriedToRemote();

        for(int j = 0; j < ((AccessorAbstractContainerMenu)this).getDataSlots().size(); ++j) {
            DataSlot dataslot = ((AccessorAbstractContainerMenu)this).getDataSlots().get(j);
            int k = dataslot.get();
            if (dataslot.checkAndClearUpdateFlag()) {
                ((AccessorAbstractContainerMenu)this).callUpdateDataSlotListeners(j, k);
            }

            ((AccessorAbstractContainerMenu)this).callSynchronizeDataSlotToRemote(j, k);
        }

    }


}
