package team.lodestar.lodestone;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.lodestar.lodestone.compability.CuriosCompat;
import team.lodestar.lodestone.compability.JeiCompat;
import team.lodestar.lodestone.config.ClientConfig;
import team.lodestar.lodestone.data.LodestoneBlockTagDatagen;
import team.lodestar.lodestone.data.LodestoneItemTagDatagen;
import team.lodestar.lodestone.data.LodestoneLangDatagen;
import team.lodestar.lodestone.registry.common.LodestoneAttributeRegistry;
import team.lodestar.lodestone.registry.common.LodestoneBlockEntityRegistry;
import team.lodestar.lodestone.registry.common.LodestonePaintingRegistry;
import team.lodestar.lodestone.registry.common.LodestoneRecipeSerializerRegistry;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;

@Mod(LodestoneLib.LODESTONE)
public class LodestoneLib {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String LODESTONE = "lodestone";
	public static final RandomSource RANDOM = RandomSource.create();

	public LodestoneLib() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

		LodestoneBlockEntityRegistry.BLOCK_ENTITY_TYPES.register(modBus);
		LodestoneParticleRegistry.PARTICLES.register(modBus);
		LodestoneAttributeRegistry.ATTRIBUTES.register(modBus);
		LodestoneRecipeSerializerRegistry.RECIPE_SERIALIZERS.register(modBus);
		LodestonePaintingRegistry.register(modBus);


		JeiCompat.init();
		CuriosCompat.init();

		modBus.addListener(this::gatherData);
	}

	public static ResourceLocation lodestonePath(String path) {
		return new ResourceLocation(LODESTONE, path);
	}

	public void gatherData(GatherDataEvent event) {
		event.getGenerator().addProvider(true, new LodestoneLangDatagen(event.getGenerator().getPackOutput()));
		LodestoneBlockTagDatagen blockTagDatagen = new LodestoneBlockTagDatagen(event.getGenerator().getPackOutput(), event.getLookupProvider(), LODESTONE, event.getExistingFileHelper());
		event.getGenerator().addProvider(true, blockTagDatagen);
		event.getGenerator().addProvider(true, new LodestoneItemTagDatagen(event.getGenerator().getPackOutput(), event.getLookupProvider(), blockTagDatagen.contentsGetter(), LODESTONE, event.getExistingFileHelper()));
	}
}