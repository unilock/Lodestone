package team.lodestar.lodestone.systems.particle.world.behaviors;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import team.lodestar.lodestone.systems.particle.world.*;
import team.lodestar.lodestone.systems.particle.world.behaviors.components.*;

public interface LodestoneParticleBehavior {

    LodestoneParticleBehavior BILLBOARD = new BillboardParticleBehavior();
    LodestoneParticleBehavior SPARK = new SparkParticleBehavior();
    LodestoneParticleBehavior DIRECTIONAL = new DirectionalParticleBehavior();

    void render(LodestoneWorldParticle particle, VertexConsumer consumer, Camera camera, float partialTicks);

    LodestoneBehaviorComponent getComponent(LodestoneBehaviorComponent component);
}
