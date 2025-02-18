package team.lodestar.lodestone.network.interaction;

import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import team.lodestar.lodestone.component.LodestoneComponents;
import team.lodestar.lodestone.events.LodestoneInteractionEvent;
import team.lodestar.lodestone.systems.network.LodestoneServerPacket;

public class UpdateRightClickPacket extends LodestoneServerPacket {

    private final boolean rightClickHeld;

    public UpdateRightClickPacket(boolean rightClick) {
        this.rightClickHeld = rightClick;
    }

    public UpdateRightClickPacket(FriendlyByteBuf buf) {
        rightClickHeld = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(rightClickHeld);
    }

    @Override
    public void executeServer(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel) {
        if (rightClickHeld) {
            LodestoneInteractionEvent.RIGHT_CLICK_EMPTY.invoker().onRightClickEmpty(player);
        }
        LodestoneComponents.LODESTONE_PLAYER_COMPONENT.maybeGet(player).ifPresent(c -> c.rightClickHeld = rightClickHeld);
    }

    public static UpdateRightClickPacket decode(FriendlyByteBuf buf) {
        return new UpdateRightClickPacket(buf.readBoolean());
    }
}