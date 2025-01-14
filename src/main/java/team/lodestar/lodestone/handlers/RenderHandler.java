package team.lodestar.lodestone.handlers;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30C;
import team.lodestar.lodestone.*;
import team.lodestar.lodestone.config.ClientConfig;
import team.lodestar.lodestone.helpers.RenderHelper;
import team.lodestar.lodestone.systems.rendering.rendeertype.ShaderUniformHandler;
import team.lodestar.lodestone.systems.rendering.shader.ExtendedShaderInstance;

import java.util.*;

import static team.lodestar.lodestone.systems.rendering.StateShards.NORMAL_TRANSPARENCY;

/**
 * A handler responsible for all the backend rendering processes.
 * To have additive transparency work in a minecraft environment, we need to buffer our rendering till after clouds and water have rendered.
 * This happens for particles, as well as all of our custom RenderTypes
 */
public class RenderHandler {
    public static final HashMap<RenderType, BufferBuilder> BUFFERS = new HashMap<>();
    public static final HashMap<RenderType, BufferBuilder> PARTICLE_BUFFERS = new HashMap<>();
    public static final HashMap<RenderType, BufferBuilder> LATE_BUFFERS = new HashMap<>();
    public static final HashMap<RenderType, BufferBuilder> LATE_PARTICLE_BUFFERS = new HashMap<>();

    public static final HashMap<RenderType, ShaderUniformHandler> UNIFORM_HANDLERS = new HashMap<>();
    public static final Collection<RenderType> TRANSPARENT_RENDER_TYPES = new ArrayList<>();

    //public static boolean LARGER_BUFFER_SOURCES = FabricLoader.getInstance().isModLoaded("sodium");
    public static RenderTarget LODESTONE_DEPTH_CACHE;


    public static LodestoneRenderLayer DELAYED_RENDER;
    public static LodestoneRenderLayer LATE_DELAYED_RENDER;

    public static PoseStack MAIN_POSE_STACK;
    public static Matrix4f MATRIX4F;

    public static float FOG_NEAR;
    public static float FOG_FAR;
    public static FogShape FOG_SHAPE;
    public static float FOG_RED, FOG_GREEN, FOG_BLUE;

    public static void onClientSetup() {
        DELAYED_RENDER = new LodestoneRenderLayer(BUFFERS, PARTICLE_BUFFERS);
        LATE_DELAYED_RENDER = new LodestoneRenderLayer(LATE_BUFFERS, LATE_PARTICLE_BUFFERS);
    }

    public static void resize(int width, int height) {
        if (LODESTONE_DEPTH_CACHE != null) {
            LODESTONE_DEPTH_CACHE.resize(width, height, Minecraft.ON_OSX);
        }
    }

    public static void endBatches() {
        copyDepthBuffer(LODESTONE_DEPTH_CACHE);
        endBatches(DELAYED_RENDER);
        endBatches(LATE_DELAYED_RENDER);
    }

    public static void endBatches(LodestoneRenderLayer renderLayer) {
        Matrix4f last = new Matrix4f(RenderSystem.getModelViewMatrix());
        beginBufferedRendering();
        renderBufferedParticles(renderLayer, true);
        if (RenderHandler.MATRIX4F != null) {
            RenderSystem.getModelViewMatrix().set(MATRIX4F);
        }
        renderBufferedBatches(renderLayer, true);
        renderBufferedBatches(renderLayer, false);
        RenderSystem.getModelViewMatrix().set(last);
        renderBufferedParticles(renderLayer, false);

        endBufferedRendering();
    }

    public static void cacheFogData(float start, float end, FogShape shape) {
        FOG_NEAR = start;
        FOG_FAR = end;
        FOG_SHAPE = shape;
    }

    public static void cacheFogData(float fogRed, float fogGreen, float fogBlue) {
        FOG_RED = fogRed;
        FOG_GREEN = fogGreen;
        FOG_BLUE = fogBlue;
    }

    public static void beginBufferedRendering() {
        float[] shaderFogColor = RenderSystem.getShaderFogColor();
        float fogRed = shaderFogColor[0];
        float fogGreen = shaderFogColor[1];
        float fogBlue = shaderFogColor[2];
        float shaderFogStart = RenderSystem.getShaderFogStart();
        float shaderFogEnd = RenderSystem.getShaderFogEnd();
        FogShape shaderFogShape = RenderSystem.getShaderFogShape();

        RenderSystem.setShaderFogStart(FOG_NEAR);
        RenderSystem.setShaderFogEnd(FOG_FAR);
        RenderSystem.setShaderFogShape(FOG_SHAPE);
        RenderSystem.setShaderFogColor(FOG_RED, FOG_GREEN, FOG_BLUE);

        FOG_RED = fogRed;
        FOG_GREEN = fogGreen;
        FOG_BLUE = fogBlue;

        FOG_NEAR = shaderFogStart;
        FOG_FAR = shaderFogEnd;
        FOG_SHAPE = shaderFogShape;
    }

    public static void endBufferedRendering() {
        RenderSystem.setShaderFogStart(FOG_NEAR);
        RenderSystem.setShaderFogEnd(FOG_FAR);
        RenderSystem.setShaderFogShape(FOG_SHAPE);
        RenderSystem.setShaderFogColor(FOG_RED, FOG_GREEN, FOG_BLUE);
    }

    public static void renderBufferedParticles(LodestoneRenderLayer renderLayer, boolean transparentOnly) {
        renderBufferedBatches(renderLayer.particleTarget, PARTICLE_BUFFERS, transparentOnly);
    }

    public static void renderBufferedBatches(LodestoneRenderLayer renderLayer, boolean transparentOnly) {
        renderBufferedBatches(renderLayer.target, BUFFERS, transparentOnly);
    }

    private static void renderBufferedBatches(MultiBufferSource.BufferSource bufferSource, HashMap<RenderType, BufferBuilder> buffer, boolean transparentOnly) {
        if (transparentOnly) {
            endBatches(bufferSource, TRANSPARENT_RENDER_TYPES);
        } else {
            Collection<RenderType> nonTransparentRenderTypes = new ArrayList<>(buffer.keySet());
            nonTransparentRenderTypes.removeIf(TRANSPARENT_RENDER_TYPES::contains);
            endBatches(bufferSource, nonTransparentRenderTypes);
        }
    }

    public static void endBatches(MultiBufferSource.BufferSource source, Collection<RenderType> renderTypes) {
        for (RenderType type : renderTypes) {
            ShaderInstance instance = RenderHelper.getShader(type);
            if (UNIFORM_HANDLERS.containsKey(type)) {
                ShaderUniformHandler handler = UNIFORM_HANDLERS.get(type);
                handler.updateShaderData(instance);
            }
            if (LODESTONE_DEPTH_CACHE != null) {
                instance.setSampler("SceneDepthBuffer", LODESTONE_DEPTH_CACHE.getDepthTextureId());
                instance.safeGetUniform("InvProjMat").set(new Matrix4f(RenderSystem.getProjectionMatrix()).invert());
            }

            source.endBatch(type);
            if (instance instanceof ExtendedShaderInstance extendedShaderInstance) {
                extendedShaderInstance.setUniformDefaults();
            }
        }
    }

    //TODO: offer some actual option here to decide if particle or not
    public static void addRenderType(RenderType renderType) {
        final boolean isParticle = renderType.name.contains("particle");
        HashMap<RenderType, BufferBuilder> buffers = isParticle ? PARTICLE_BUFFERS : BUFFERS;
        HashMap<RenderType, BufferBuilder> lateBuffers = isParticle ? LATE_PARTICLE_BUFFERS : LATE_BUFFERS;
        buffers.put(renderType, new BufferBuilder(renderType.bufferSize()));
        lateBuffers.put(renderType, new BufferBuilder(renderType.bufferSize()));
        if (NORMAL_TRANSPARENCY.equals(RenderHelper.getTransparencyShard(renderType))) {
            TRANSPARENT_RENDER_TYPES.add(renderType);
        }
    }

    public static void copyDepthBuffer(RenderTarget tempRenderTarget) {
        if (tempRenderTarget == null) return;
        RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        tempRenderTarget.copyDepthFrom(mainRenderTarget);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mainRenderTarget.frameBufferId);
    }

    public static class LodestoneRenderLayer {

        protected final HashMap<RenderType, BufferBuilder> buffers;
        protected final HashMap<RenderType, BufferBuilder> particleBuffers;

        protected final MultiBufferSource.BufferSource target;
        protected final MultiBufferSource.BufferSource particleTarget;

        public LodestoneRenderLayer(HashMap<RenderType, BufferBuilder> buffers, HashMap<RenderType, BufferBuilder> particleBuffers) {
            this(buffers, particleBuffers, FabricLoader.getInstance().isModLoaded("sodium") ? 2097152 : 256);
        }
        public LodestoneRenderLayer(HashMap<RenderType, BufferBuilder> buffers, HashMap<RenderType, BufferBuilder> particleBuffers, int size) {
            this.buffers = buffers;
            this.particleBuffers = particleBuffers;
            this.target = MultiBufferSource.immediateWithBuffers(buffers, new BufferBuilder(size));
            this.particleTarget = MultiBufferSource.immediateWithBuffers(particleBuffers, new BufferBuilder(size));
        }

        public HashMap<RenderType, BufferBuilder> getBuffers() {
            return buffers;
        }

        public HashMap<RenderType, BufferBuilder> getParticleBuffers() {
            return particleBuffers;
        }

        public MultiBufferSource.BufferSource getTarget() {
            return target;
        }

        public MultiBufferSource.BufferSource getParticleTarget() {
            return particleTarget;
        }
    }
}