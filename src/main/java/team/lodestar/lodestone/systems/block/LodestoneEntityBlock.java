package team.lodestar.lodestone.systems.block;

import io.github.fabricators_of_create.porting_lib.blocks.extensions.OnExplodedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import team.lodestar.lodestone.systems.blockentity.LodestoneBlockEntity;

import java.util.function.Supplier;

/**
 * A SimpleBlock is an implementation of EntityBlock that allows most frequently used logic to be handled in a SimpleBlockEntity
 * It's important to still utilize generic, T extends YourBlockEntity, in order to allow for other mods to extend your block and use a different block entity
 */
@SuppressWarnings("unchecked")
public class LodestoneEntityBlock<T extends LodestoneBlockEntity> extends Block implements EntityBlock, OnExplodedBlock {

    protected Supplier<BlockEntityType<T>> blockEntityType = null;
    protected BlockEntityTicker<T> ticker = null;

    public LodestoneEntityBlock(Properties properties) {
        super(properties);
    }

    public LodestoneEntityBlock<T> setBlockEntity(Supplier<BlockEntityType<T>> type) {
        this.blockEntityType = type;
        this.ticker = (l, p, s, t) -> t.tick();
        return this;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return hasTileEntity(state) ? blockEntityType.get().create(pos, state) : null;
    }

    public boolean hasTileEntity(BlockState state) {
        return this.blockEntityType != null;
    }

    @Override
    public <Y extends BlockEntity> BlockEntityTicker<Y> getTicker(Level level, BlockState state, BlockEntityType<Y> type) {
        return (BlockEntityTicker<Y>) ticker;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (hasTileEntity(pState)) {
            if (pLevel.getBlockEntity(pPos) instanceof LodestoneBlockEntity simpleBlockEntity) {
                simpleBlockEntity.onPlace(pPlacer, pStack);
            }
        }
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (hasTileEntity(state)) {
            if (level.getBlockEntity(pos) instanceof LodestoneBlockEntity simpleBlockEntity) {
                ItemStack stack = simpleBlockEntity.onClone(state, level, pos);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        onBlockBroken(state, level, pos, player);
        super.playerWillDestroy(level, pos, state, player);
    }


    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        onBlockBroken(state, level, pos, null);
        OnExplodedBlock.super.onBlockExploded(state, level, pos, explosion);
    }



    public void onBlockBroken(BlockState state, BlockGetter level, BlockPos pos, @Nullable Player player) {
        if (hasTileEntity(state)) {
            if (level.getBlockEntity(pos) instanceof LodestoneBlockEntity simpleBlockEntity) {
                simpleBlockEntity.onBreak(player);
            }
        }
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (hasTileEntity(pState)) {
            if (pLevel.getBlockEntity(pPos) instanceof LodestoneBlockEntity simpleBlockEntity) {
                simpleBlockEntity.onEntityInside(pState, pLevel, pPos, pEntity);
            }
        }
        super.entityInside(pState, pLevel, pPos, pEntity);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (hasTileEntity(pState)) {
            if (pLevel.getBlockEntity(pPos) instanceof LodestoneBlockEntity simpleBlockEntity) {
                simpleBlockEntity.onNeighborUpdate(pState, pPos, pFromPos);
            }
        }
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        if (hasTileEntity(state)) {
            if (level.getBlockEntity(pos) instanceof LodestoneBlockEntity simpleBlockEntity) {
                return simpleBlockEntity.onUse(player, hand);
            }
        }
        return super.use(state, level, pos, player, hand, ray);
    }
}
