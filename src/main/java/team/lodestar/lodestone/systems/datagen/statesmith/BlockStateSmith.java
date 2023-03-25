package team.lodestar.lodestone.systems.datagen.statesmith;

import net.minecraft.world.level.block.*;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.registries.RegistryObject;
import team.lodestar.lodestone.LodestoneLib;

import java.util.Collection;

public class BlockStateSmith<T extends Block> extends AbstractBlockStateSmith<T> {

    public final SmithStateSupplier<T> stateSupplier;

    public BlockStateSmith(Class<T> blockClass, SmithStateSupplier<T> stateSupplier) {
        super(blockClass);
        this.stateSupplier = stateSupplier;
    }

    @SafeVarargs
    public final void act(BlockStateProvider provider, RegistryObject<Block>... blocks) {
        for (RegistryObject<Block> block : blocks) {
            act(provider, block.get());
        }
    }

    public void act(BlockStateProvider provider, Collection<RegistryObject<Block>> blocks) {
        blocks.forEach(r -> act(provider, r.get()));
    }

    public void act(BlockStateProvider provider, Block block) {
        if (blockClass.isInstance(block)) {
            stateSupplier.act(blockClass.cast(block), provider);
        } else {
            LodestoneLib.LOGGER.warn("Block does not match the state smith it was assigned: " + block.getRegistryName());
        }
    }

    interface SmithStateSupplier<T extends Block> {
        void act(T block, BlockStateProvider provider);
    }
}