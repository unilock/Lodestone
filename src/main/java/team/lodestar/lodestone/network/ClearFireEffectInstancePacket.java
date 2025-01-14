package team.lodestar.lodestone.network;

import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import team.lodestar.lodestone.handlers.FireEffectHandler;
import team.lodestar.lodestone.systems.network.LodestoneClientPacket;

public class ClearFireEffectInstancePacket extends LodestoneClientPacket {

    private final int entityID;

    public ClearFireEffectInstancePacket(int entityID) {
        this.entityID = entityID;
    }

    public ClearFireEffectInstancePacket(FriendlyByteBuf buf) {
        entityID = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityID);
    }

    @Override
    public void executeClient(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        FireEffectHandler.setCustomFireInstance(Minecraft.getInstance().level.getEntity(entityID), null);
    }
}