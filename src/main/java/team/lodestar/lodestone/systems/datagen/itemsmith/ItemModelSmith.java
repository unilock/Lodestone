package team.lodestar.lodestone.systems.datagen.itemsmith;

import net.minecraft.world.item.Item;
import team.lodestar.lodestone.systems.datagen.providers.LodestoneItemModelProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ItemModelSmith extends AbstractItemModelSmith {

    public final ItemModelSupplier modelSupplier;

    public ItemModelSmith(ItemModelSupplier modelSupplier) {
        this.modelSupplier = modelSupplier;
    }

    @SafeVarargs
    public final void act(ItemModelSmithData data, Supplier<? extends Item>... items) {
        for (Supplier<? extends Item> item : items) {
            act(data, item);
        }
        List.of(items).forEach(data.consumer);
    }

    public void act(ItemModelSmithData data, Collection<Supplier<? extends Item>> items) {
        items.forEach(r -> act(data, r));
        new ArrayList<>(items).forEach(data.consumer);
    }

    private void act(ItemModelSmithData data, Supplier<? extends Item> registryObject) {
        Item item = registryObject.get();
        modelSupplier.act(item, data.provider);
    }

    public void act(Supplier<? extends Item> registryObject, LodestoneItemModelProvider provider) {
        Item item = registryObject.get();
        modelSupplier.act(item, provider);
    }

    public interface ItemModelSupplier {
        void act(Item item, LodestoneItemModelProvider provider);
    }
}