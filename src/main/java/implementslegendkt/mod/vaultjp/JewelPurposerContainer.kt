package implementslegendkt.mod.vaultjp

import com.google.common.base.Suppliers
import implementslegendkt.mod.vaultjp.mixin.AccessorAbstractContainerMenu
import implementslegendkt.mod.vaultjp.mixin.AccessorOverSizedInventory
import iskallia.vault.container.oversized.OverSizedSlotContainer
import iskallia.vault.container.slot.TabSlot
import iskallia.vault.init.ModSlotIcons
import iskallia.vault.item.tool.JewelItem
import iskallia.vault.item.tool.ToolItem
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.common.extensions.IForgeMenuType
import java.util.function.Supplier

class JewelPurposerContainer(windowId: Int, world: Level, private val tilePos: BlockPos, playerInventory: Inventory) :
    OverSizedSlotContainer(
        MENU_TYPE, windowId, playerInventory.player
    ) {
    val tileEntity: JewelPurposerBlockEntity

    init {
        val tile = world.getBlockEntity(this.tilePos)
        this.tileEntity = (tile as? JewelPurposerBlockEntity)!!
        initSlots(playerInventory)
    }

    private fun initSlots(playerInventory: Inventory) {
        repeat (3) { row->
            repeat(9) { column->
                this.addSlot(TabSlot(playerInventory, column + row * 9 + 9, 58 + column * 18, 108 + row * 18))
            }
        }

        repeat(9) {hotbarSlot->
            this.addSlot(TabSlot(playerInventory, hotbarSlot, 58 + hotbarSlot * 18, 166))
        }

        val invContainer: Container = tileEntity.inventory

        repeat (256) {row->
            repeat(96) {column->
                val overrideIndex = row * 96 + column
                this.addSlot(object : TabSlot(invContainer, overrideIndex, -999 + column * 18, 50 + row * 18) {
                    override fun mayPlace(stack: ItemStack): Boolean {
                        return stack.item is JewelItem
                    }

                    override fun getItem(): ItemStack {
                        val itemstack0 =
                            (invContainer as AccessorOverSizedInventory).contentsOverSized!![overrideIndex]
                        val itemstack = ItemStack(itemstack0.stack().item, itemstack0.amount())
                        itemstack.tag = itemstack0.stack().tag
                        return itemstack
                    }
                })
            }
        }

        repeat (4) {row->
            repeat(4) {column->
                /*
                * Extra slots intended for other purposes; currently unused
                * */
                this.addSlot(object :
                    TabSlot(invContainer, row * 4 + column + JEWEL_COUNT_MAX, -999 + column * 18, 50 + row * 18) {
                    override fun mayPlace(stack: ItemStack): Boolean {
                        return false //!(stack.getItem() instanceof JewelItem) && !(stack.getItem() instanceof ToolItem);
                    }
                })
            }
        }
        this.addSlot(object : TabSlot(invContainer, JEWEL_COUNT_MAX + 16, 120, 73) {
            override fun mayPlace(stack: ItemStack): Boolean {
                return stack.item is ToolItem
            }
        }.setBackground(InventoryMenu.BLOCK_ATLAS, ModSlotIcons.TOOL_NO_ITEM))
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var itemstack = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasItem()) {
            val slotStack = slot.item
            itemstack = slotStack.copy()
            if (index in 0..35 && this.moveOverSizedItemStackTo(
                    slotStack, slot, 36,
                    slots.size, false
                )
            ) {
                return itemstack
            }

            when {
                index in 0..26 -> {
                    if (!this.moveOverSizedItemStackTo(slotStack, slot, 27, 36, false)) {
                        return ItemStack.EMPTY
                    }
                }
                index in 27..35 -> {
                    if (!this.moveOverSizedItemStackTo(slotStack, slot, 0, 27, false)) {
                        return ItemStack.EMPTY
                    }
                }
                !this.moveOverSizedItemStackTo(slotStack, slot, 0, 36, false) -> {
                    return ItemStack.EMPTY
                }
            }

            if (slotStack.count == 0) {
                slot.set(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (slotStack.count == itemstack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(player, slotStack)
        }

        return itemstack
    }

    override fun stillValid(player: Player): Boolean = tileEntity.stillValid(this.player)


    //speed up!
    override fun broadcastChanges() {
        (this as AccessorAbstractContainerMenu).apply {

            for (i in slots.indices) {
                val itemstack = slots[i].item
                val supplier: Supplier<ItemStack?> = Suppliers.memoize { itemstack }
                this.callTriggerSlotListeners(i, itemstack, supplier)
                this.callSynchronizeSlotToRemote(i, itemstack, supplier)
            }
            this.callSynchronizeCarriedToRemote()

            for (j in this.dataSlots!!.indices) {
                val dataslot = this.dataSlots!![j]!!
                val k = dataslot.get()
                if (dataslot.checkAndClearUpdateFlag()) this.callUpdateDataSlotListeners(j, k)

                this.callSynchronizeDataSlotToRemote(j, k)
            }
        }
    }


    companion object {
        @JvmField
        val MENU_TYPE: MenuType<JewelPurposerContainer> =
            IForgeMenuType.create { windowId: Int, inv: Inventory, data: FriendlyByteBuf ->
                val world = inv.player.level
                val pos = data.readBlockPos()
                JewelPurposerContainer(windowId, world, pos, inv)
            }
        const val JEWEL_COUNT_MAX: Int = 256 * 96
    }
}
