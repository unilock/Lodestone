package team.lodestar.lodestone.network;

import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import team.lodestar.lodestone.systems.network.LodestoneClientPacket;

public class TotemOfUndyingEffectPacket extends LodestoneClientPacket {
    private final int entityId;
    private final ItemStack stack;

    public TotemOfUndyingEffectPacket(Entity entity, ItemStack stack) {
        this.entityId = entity.getId();
        this.stack = stack;
    }

    public TotemOfUndyingEffectPacket(int entityId, ItemStack stack) {
        this.entityId = entityId;
        this.stack = stack;
    }

    public TotemOfUndyingEffectPacket(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        stack = buf.readItem();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeItem(stack);
    }

    @Override
    public void executeClient(Minecraft minecraft, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        Entity entity = minecraft.level.getEntity(entityId);
        if (entity instanceof LivingEntity livingEntity) {
            minecraft.particleEngine.createTrackingEmitter(livingEntity, ParticleTypes.TOTEM_OF_UNDYING, 30);
            minecraft.level.playLocalSound(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.TOTEM_USE, livingEntity.getSoundSource(), 1.0F, 1.0F, false);
            if (livingEntity == minecraft.player) {
                minecraft.gameRenderer.displayItemActivation(stack);
            }
        }
    }

    public static TotemOfUndyingEffectPacket decode(FriendlyByteBuf buf) {
        return new TotemOfUndyingEffectPacket(buf.readInt(), buf.readItem());
    }
}