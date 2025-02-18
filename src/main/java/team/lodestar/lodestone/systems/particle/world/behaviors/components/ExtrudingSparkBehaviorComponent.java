package team.lodestar.lodestone.systems.particle.world.behaviors.components;

import net.minecraft.world.phys.*;
import team.lodestar.lodestone.systems.particle.data.*;
import team.lodestar.lodestone.systems.particle.world.*;
import team.lodestar.lodestone.systems.particle.world.behaviors.*;

public class ExtrudingSparkBehaviorComponent extends SparkBehaviorComponent {

    public ExtrudingSparkBehaviorComponent(GenericParticleData lengthData) {
        super(lengthData);
    }

    public ExtrudingSparkBehaviorComponent() {
        this(null);
    }

    @Override
    public Vec3 sparkStart(Vec3 pos, Vec3 offset) {
        return pos;
    }

    @Override
    public Vec3 sparkEnd(Vec3 pos, Vec3 offset) {
        return pos.add(offset.x * 2, offset.y * 2, offset.z * 2);
    }
}