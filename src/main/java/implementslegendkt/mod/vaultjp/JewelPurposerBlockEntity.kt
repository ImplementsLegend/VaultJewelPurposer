package implementslegendkt.mod.vaultjp

import implementslegendkt.mod.vaultjp.PurposerExternalStorages.getOrCreateExternalStorage
import implementslegendkt.mod.vaultjp.PurposerExternalStorages.saveExternalStorage
import implementslegendkt.mod.vaultjp.mixin.AccessorDataStorage
import implementslegendkt.mod.vaultjp.network.Channel
import implementslegendkt.mod.vaultjp.network.UpdatePurposesPacket
import iskallia.vault.container.oversized.OverSizedInventory
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger
import iskallia.vault.gear.data.ToolGearData
import iskallia.vault.init.ModGearAttributes
import iskallia.vault.item.JewelPouchItem
import iskallia.vault.item.tool.JewelItem
import iskallia.vault.item.tool.ToolItem
import iskallia.vault.util.LootInitialization
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.InvWrapper
import java.util.*

class JewelPurposerBlockEntity(p_155229_: BlockPos?, p_155230_: BlockState?) : BlockEntity(TYPE, p_155229_, p_155230_),
    MenuProvider {


    private var data: PurposerExternalStorage? = null

    private var handler: LazyOptional<out IItemHandler> = LazyOptional.of { InvWrapper(getOrCreateData().inventory) }

    val inventory get() = getOrCreateData().inventory
    val purposes get() = getOrCreateData().purposes

    var externalUUID= UUID.randomUUID()
    
    var oldTag:CompoundTag? = null

    private fun getOrCreateData(): PurposerExternalStorage{
        val _oldTag = oldTag
        oldTag=null
        return data?:run {
            val result = ((level as? ServerLevel)?.let {
                getOrCreateExternalStorage(
                    it.server.overworld().getDataStorage(),
                    externalUUID,
                    this
                )
            } ?: PurposerExternalStorage(externalUUID, this)).also { data = it }

            if(_oldTag!==null){
                result.inventory.load(_oldTag)
                result.purposes.addAll(
                    _oldTag.getList("purposes", CompoundTag.TAG_COMPOUND.toInt())
                        .map { JewelPurpose.readNBT(it as CompoundTag) })

            }
            result
        }
    }

    override fun getDisplayName(): Component = TextComponent("Jewel Purposer")

    override fun createMenu(p_39954_: Int, p_39955_: Inventory, p_39956_: Player): AbstractContainerMenu? =
        this.getLevel()?.let {
            JewelPurposerContainer(
                p_39954_,
                it,
                this.blockPos,
                p_39956_.inventory
            )
        }

    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> =
        if (!this.remove && cap === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) handler.cast()
            else super.getCapability(cap, side)

    override fun invalidateCaps() {
        super.invalidateCaps()
        handler.invalidate()
    }

    override fun reviveCaps() {
        super.reviveCaps()
        this.handler = LazyOptional.of { InvWrapper(inventory) }
    }


    override fun load(tag: CompoundTag) {
        super.load(tag)

        if (tag.hasUUID("externalUUID")) externalUUID = tag.getUUID("externalUUID")
        else oldTag=tag
        //inventory.load(tag)
    }

    public override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        val _data = data
        tag.putUUID("externalUUID", externalUUID)
        if (_data != null) (level as? ServerLevel)?.let {
            saveExternalStorage(it.dataStorage,_data)
        }
        //inventory.save(tag)
    }


    override fun getUpdateTag(): CompoundTag = this.saveWithoutMetadata()

    override fun getUpdatePacket(): ClientboundBlockEntityDataPacket? = ClientboundBlockEntityDataPacket.create(this)


    fun stillValid(player: Player?): Boolean =
        this.level != null &&
                (level!!.getBlockEntity(this.worldPosition) === this) &&
                inventory.stillValid(player)


    fun applyJewelsMock(tool: ItemStack, jewels: IntArray) = jewels.fold(tool){
        toolAcc,jewelIndex->
        if(jewelIndex !in 0 until JewelPurposerContainer.JEWEL_COUNT_MAX) return@applyJewelsMock toolAcc
        ToolItem.applyJewel(toolAcc,inventory.getItem(jewelIndex), false)
        toolAcc
    }

    fun applyJewels(jewels: IntArray) {
        val toolIndex = inventory.containerSize - 1
        var toolOld = inventory.removeItemNoUpdate(toolIndex)
        var tool: ItemStack
        if (toolOld.item !is ToolItem) {
            inventory.setItem(toolIndex, toolOld)
            return
        }

        for (jew in jewels) {
            if (jew >= 0 && jew < JewelPurposerContainer.JEWEL_COUNT_MAX) {
                if (ToolGearData.read(toolOld)
                        .get(ModGearAttributes.TOOL_CAPACITY, VaultGearAttributeTypeMerger.intSum()) < 10
                ) break
                tool = toolOld.copy()
                val jewel = inventory.removeItemNoUpdate(jew)

                if (ToolItem.applyJewel(tool,jewel,true) && ToolGearData.read(tool)
                        .get<Int, Int>(ModGearAttributes.TOOL_CAPACITY, VaultGearAttributeTypeMerger.intSum()) < 0
                ) {
                    inventory.setItem(jew, jewel)
                } else {
                    toolOld = tool
                }
            }
        }

        inventory.setItem(toolIndex, toolOld)
    }

    fun tick() {
        if (level!!.isClientSide()) return
        disposeBad()
    }

    private fun tryRecycleJewel(slot: Int, cap: IItemHandler): Boolean {
        if (purposes.isEmpty() || slot < 0 || slot >= JewelPurposerContainer.JEWEL_COUNT_MAX) return false
        val jewel = inventory.getItem(slot)
        if (purposes.any { !it.isBad(jewel) } || jewel.item !is JewelItem) return true

        var stack = inventory.getItem(slot)
        for (targetSlot in 0 until cap.slots) {
            if (stack.isEmpty) break
            stack = cap.insertItem(targetSlot, stack, false)
        }
        if (stack.isEmpty) { //assuming 1 item per slot
            inventory.removeItemNoUpdate(slot)
            inventory.setChanged()
        } else return false
        return true
    }

    fun disposeBad() {
        if (purposes.isEmpty()) return

        val next = level?.getBlockEntity(worldPosition.below()) ?: return
        val cap = next.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP)
            .orElse(next.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null))
            ?: return

        repeat (JewelPurposerContainer.JEWEL_COUNT_MAX) {
            if (!tryRecycleJewel(it, cap)) return
        }
    }

    fun syncToServer() = Channel.CHANNEL.sendToServer(UpdatePurposesPacket(blockPos, purposes))

    fun deleteExternalData() {
        data?.deleted=true
        ((level as? ServerLevel)?.dataStorage as? AccessorDataStorage)?.callGetDataFile(PurposerExternalStorages.nameFor(externalUUID))?.delete()
    }

    fun acceptBest(options: List<JewelPouchItem.RolledJewel>,level:Int,dbgPlayer:ServerPlayer?=null) {
        (this.level as? ServerLevel)?.let {
            world->
            val identified = options.filter { it.identified }.map { it.stack }
            val unidentified = options.filter { !it.identified }.map { LootInitialization.initializeVaultLoot(it.stack, level)/*todo identify*/ }.toMutableList()

                    val chosen = (identified.mapNotNull {
                    jewel->
                val ratios = purposes.map {
                    it.getJewelUsefulness(jewel)/it.disposeThreshold
                }
                val highestRatio = ratios.maxOrNull()?:run { unidentified+=jewel;return@mapNotNull null }
                (jewel to highestRatio).takeIf { it.second>1.0 }
            }.maxByOrNull {
                it.second
            }?.first?:unidentified.random()).copy()

            val slot = run slot@{
                repeat(JewelPurposerContainer.JEWEL_COUNT_MAX) {
                    if(inventory.getItem(it).isEmpty)return@slot it
                }
                -1
            }
            if(slot!=-1){
                inventory.setItem(slot,chosen)
            }else{
                world.addFreshEntity(ItemEntity(world, worldPosition.x.toDouble(),
                    worldPosition.y.toDouble(), worldPosition.z.toDouble(),chosen))
            }
        }
    }


    companion object {
        @JvmField
        val TYPE: BlockEntityType<JewelPurposerBlockEntity> = BlockEntityType.Builder.of(
            { pos, state -> JewelPurposerBlockEntity(pos, state) },
            JewelPurposerBlock
        ).build(null)
    }
}


class JewelPurposerInventory(val purposer:JewelPurposerBlockEntity) :
    OverSizedInventory(JewelPurposerContainer.JEWEL_COUNT_MAX + 16 + 1, purposer),
    DefaultWorldlyContainer
{
    override fun canTakeItemThroughFace(p_19235_: Int, stack: ItemStack, directio: Direction): Boolean =
        if (stack.item is JewelItem && purposer.purposes.any { purpose -> purpose.getJewelUsefulness(stack) >= purpose.disposeThreshold }) {
            false
        } else super.canPlaceItemThroughFace(p_19235_, stack, directio)
}
