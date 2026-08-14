package com.bretzelfresser.dinosexpansion.datagen;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, DinosExpansion.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.NARCOTICS.get());
        basicItem(ModItems.TRANQUILIZER_ARROW.get());
        kibble(ModItems.BASIC_KIBBLE);
        kibble(ModItems.SIMPLE_KIBBLE);
        kibble(ModItems.REGULAR_KIBBLE);
        kibble(ModItems.SUPERIOR_KIBBLE);
        kibble(ModItems.EXCEPTIONAL_KIBBLE);
        kibble(ModItems.EXTRAORDINARY_KIBBLE);
        dimorphodonWithPath(ModItems.RAW_DIMORPHODON, "raw_dimorphodon_meat");
        dimorphodonWithPath(ModItems.COOKED_DIMORPHODON, "cooked_dimorphodon_meat");
    }

    private void dimorphodonWithPath(Holder<Item> holder, String textureName) {
        itemWithPath(holder, "dimorphodon/" + textureName);
    }

    private void kibble(Holder<Item> holder) {
        itemWithPrefixPath(holder, "kibble");
    }

    public void itemWithPrefixPath(Holder<Item> holder, String prefixPath) {
        holder.unwrapKey().ifPresent(key -> {
            basicItem(key.location().withPrefix(prefixPath + "/"));
        });
    }

    public void itemWithPath(Holder<Item> holder, String path) {
        holder.unwrapKey().ifPresent(key -> {
            basicItem(key.location().withPath(path));
        });
    }
}
