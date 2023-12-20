package implementslegendkt.mod.vaultjp;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class JewelPurposerBlock extends Block implements EntityBlock, WorldlyContainerHolder {
    public  static final JewelPurposerBlock INSTANCE = new JewelPurposerBlock(Properties.of(Material.STONE).destroyTime(6.5f).);
    public  static final BlockItem ITEM = new BlockItem(INSTANCE, new Item.Properties());

    public JewelPurposerBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        if(player instanceof ServerPlayer serverPlayer && level.getBlockEntity(position) instanceof JewelPurposerBlockEntity entity){
            NetworkHooks.openGui(serverPlayer, entity, buffer -> buffer.writeBlockPos(position));
        }
        return InteractionResult.SUCCESS;
    }


    @javax.annotation.Nullable
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level world, BlockState state, BlockEntityType<A> type) {
        return (_level,_pos,_state,entity)->{if(entity instanceof JewelPurposerBlockEntity purposer)purposer.tick();};
    }




    public void onRemove(BlockState p_49076_, Level p_49077_, BlockPos p_49078_, BlockState p_49079_, boolean p_49080_) {
        if (!p_49076_.is(p_49079_.getBlock())) {
            var blockentity = p_49077_.getBlockEntity(p_49078_);
            if(blockentity instanceof JewelPurposerBlockEntity be) {
                Containers.dropContents(p_49077_, p_49078_, be.getInventory());
                p_49077_.updateNeighbourForOutputSignal(p_49078_, this);
            }
            super.onRemove(p_49076_, p_49077_, p_49078_, p_49079_, p_49080_);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return JewelPurposerBlockEntity.TYPE.create(p_153215_,p_153216_);
    }

    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        if(level.getBlockEntity(pos) instanceof JewelPurposerBlockEntity entity)return entity.getInventory();
        return null;
    }
}
