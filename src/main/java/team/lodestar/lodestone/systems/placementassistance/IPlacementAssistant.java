package team.lodestar.lodestone.systems.placementassistance;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Predicate;

/**
 * A placement assistant is a helpful client-sided system which allows you to do custom things when a player places something.
 */
public interface IPlacementAssistant {

    void onPlaceBlock(Player player, Level level, BlockHitResult hit, BlockState blockState, ItemStack stack);

    void onObserveBlock(Player player, Level level, BlockHitResult hit, BlockState blockState, ItemStack stack);

    Predicate<ItemStack> canAssist();
}
