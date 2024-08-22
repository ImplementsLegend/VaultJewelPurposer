package implementslegendkt.mod.vaultjp;

import iskallia.vault.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class JewelPurposerBlock extends Block implements EntityBlock, WorldlyContainerHolder {
    public  static final JewelPurposerBlock INSTANCE = new JewelPurposerBlock(Properties.of(Material.STONE).destroyTime(6.5f));
    public  static final BlockItem ITEM = new BlockItem(INSTANCE, new Item.Properties().tab(ModItems.VAULT_MOD_GROUP));

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

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // Define each leg (3x3 wide, from Y=0 to Y=12)
        // Within bounds of (14x14)
        VoxelShape legFL = Block.box(1.0D, 0.0D, 1.0D, 4.0D, 12.0D, 4.0D);  // Front-left
        VoxelShape legBL = Block.box(1.0D, 0.0D, 12.0D, 4.0D, 12.0D, 15.0D); // Back-left
        VoxelShape legFR = Block.box(12.0D, 0.0D, 1.0D, 15.0D, 12.0D, 4.0D); // Front-right
        VoxelShape legBR = Block.box(12.0D, 0.0D, 12.0D, 15.0D, 12.0D, 15.0D); // Back-right

        // Define the barrel base (10x10 wide, from Y=0 to Y=1)
        VoxelShape base = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);

        // Define the middle barrel (8x8 wide, from Y=1 to Y=12)
        VoxelShape barrelMiddle = Block.box(4.0D, 1.0D, 4.0D, 12.0D, 12.0D, 12.0D);

        // Define the tabletop (16x16 wide, from Y=12 to Y=14)
        VoxelShape tabletop = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 14.0D, 16.0D);

        // Define the top of the barrel (10x10 wide, from Y=14 to Y=15)
        VoxelShape barrelTop = Block.box(3.0D, 14.0D, 3.0D, 13.0D, 15.0D, 13.0D);

        // Combine all shapes
        return Shapes.or(base, barrelMiddle, tabletop, barrelTop, legFL, legFR, legBL, legBR);
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
