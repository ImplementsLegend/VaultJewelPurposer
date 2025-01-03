package implementslegendkt.mod.vaultjp

import implementslegendkt.mod.vaultjp.mixin.JewelPouchAccessor
import implementslegendkt.mod.vaultjp.network.Channel
import implementslegendkt.mod.vaultjp.network.UpdatePurposesPacket
import iskallia.vault.gear.VaultGearState
import iskallia.vault.init.ModItems
import iskallia.vault.item.JewelPouchItem
import iskallia.vault.skill.expertise.type.JewelExpertise
import iskallia.vault.util.InventoryUtil.findAllItemsInMainHand
import iskallia.vault.world.data.PlayerExpertisesData
import iskallia.vault.world.data.PlayerVaultStatsData
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkHooks

object JewelPurposerBlock:Block(Properties.of(Material.STONE).noOcclusion().destroyTime(6.5f)),EntityBlock,WorldlyContainerHolder {
    val ITEM = BlockItem(this, Item.Properties().tab(ModItems.VAULT_MOD_GROUP))

    init {

        this.registerDefaultState(
            stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
        )
    }

    override fun createBlockStateDefinition(p_49915_: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(p_49915_)
        p_49915_.add(BlockStateProperties.HORIZONTAL_FACING)
    }

    override fun getStateForPlacement(p_49820_: BlockPlaceContext): BlockState? {
        return super.getStateForPlacement(p_49820_)?.setValue(BlockStateProperties.HORIZONTAL_FACING,p_49820_.horizontalDirection)
    }

    override fun use(
        p_60503_: BlockState,
        p_60504_: Level,
        p_60505_: BlockPos,
        player: Player,
        p_60507_: InteractionHand,
        p_60508_: BlockHitResult
    ): InteractionResult {
        if(player is ServerPlayer) (p_60504_.getBlockEntity(p_60505_) as? JewelPurposerBlockEntity)?.let { purposer ->
            val handContent = findAllItemsInMainHand(player)
            if(handContent.size>1){
                handContent.filter {
                    it.stack.item is JewelPouchItem
                }.forEach {
                    it.stack=it.stack.apply {
                        repeat(count) { _ ->
                            val stack = copy().also { modified -> modified.count = 1 }
                            count--

                            val vaultLevel = JewelPouchItem.getStoredLevel(stack)
                                .orElseGet {
                                    PlayerVaultStatsData.get(player.getLevel()).getVaultStats(player).vaultLevel
                                }
                            if (ModItems.JEWEL_POUCH.getState(stack) !== VaultGearState.IDENTIFIED) {

                                val additionalIdentifiedJewels =
                                    PlayerExpertisesData
                                        .get(player.getLevel())
                                        .getExpertises(player)
                                        .getAll(JewelExpertise::class.java) { it.isUnlocked }
                                        .sumOf { it.additionalIdentifiedJewels }

                                (ModItems.JEWEL_POUCH as JewelPouchAccessor).callGenerateJewels(
                                    stack,
                                    vaultLevel,
                                    additionalIdentifiedJewels
                                )
                            }
                            val options = JewelPouchItem.getJewels(stack)
                            purposer.acceptBest(options, vaultLevel, dbgPlayer = player)
                        }
                    }
                }
            }else {

                Channel.CHANNEL.sendTo(
                    UpdatePurposesPacket(p_60505_, purposer.purposes),
                    player.connection.getConnection(),
                    NetworkDirection.PLAY_TO_CLIENT
                )
                NetworkHooks.openGui(player, purposer) { it.writeBlockPos(p_60505_) }
            }
        }
        return InteractionResult.SUCCESS
    }

    override fun <T : BlockEntity?> getTicker(
        p_153212_: Level,
        p_153213_: BlockState,
        p_153214_: BlockEntityType<T>
    ): BlockEntityTicker<T> = BlockEntityTicker { _, _, _, e -> (e as? JewelPurposerBlockEntity)?.tick() }

    override fun getDrops(p_60537_: BlockState, p_60538_: LootContext.Builder): MutableList<ItemStack> {

        return super.getDrops(p_60537_, p_60538_).apply {
            if(p_60538_.level.gameRules.getBoolean(Vaultjp.KEEP_CONTENT_GAMERULE))
                (p_60538_.getOptionalParameter<BlockEntity>(LootContextParams.BLOCK_ENTITY) as? JewelPurposerBlockEntity)?.let {
                    jewelPurposerBlockEntity ->
                    filter { it.item== ITEM }.forEach {
                        it.getOrCreateTagElement("BlockEntityTag").apply {
                            putString("id","vaultjp:jewel_purpuser_entity")
                            putUUID("externalUUID",jewelPurposerBlockEntity.externalUUID)
                        }
                    }
                }
        }
    }

    override fun onRemove(
        p_60515_: BlockState,
        p_60516_: Level,
        p_60517_: BlockPos,
        p_60518_: BlockState,
        p_60519_: Boolean
    ) {
        if (!p_60515_.`is`(p_60518_.getBlock())) {
            if(!p_60516_.gameRules.getBoolean(Vaultjp.KEEP_CONTENT_GAMERULE))
            (p_60516_.getBlockEntity(p_60517_) as? JewelPurposerBlockEntity)?.let {
                Containers.dropContents(p_60516_,p_60517_,it.inventory)
                it.deleteExternalData()
                p_60516_.updateNeighbourForOutputSignal(p_60517_,this)
            }
            super.onRemove(p_60515_, p_60516_, p_60517_, p_60518_, p_60519_)
        }
    }


    override fun newBlockEntity(p_153215_: BlockPos, p_153216_: BlockState)= JewelPurposerBlockEntity.TYPE.create(p_153215_,p_153216_)

    override fun getContainer(p_19242_: BlockState, p_19243_: LevelAccessor, p_19244_: BlockPos) = (p_19243_.getBlockEntity(p_19244_) as? JewelPurposerBlockEntity)?.inventory

}