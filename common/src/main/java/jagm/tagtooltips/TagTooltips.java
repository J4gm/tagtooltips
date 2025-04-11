package jagm.tagtooltips;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TagTooltips {

    public static final String MOD_ID = "tagtooltips";
    public static final KeyMapping SHOW_TAG_TOOLTIP_KEY = new KeyMapping("key." + MOD_ID + ".show_tag_tooltip", GLFW.GLFW_KEY_SEMICOLON, "key.categories.misc");

    private static final Style TITLES = Style.EMPTY.withColor(0xFFFFA0);
    private static final Style GREYED = Style.EMPTY.withColor(0xA0A0A0).withItalic(true);
    private static final Comparator<TagKey<?>> TAG_COMPARATOR = Comparator.comparing(key -> key.location().toString());

    public static void onKey(int key, boolean down){
        if(InputConstants.getKey(key, 0) != InputConstants.UNKNOWN && SHOW_TAG_TOOLTIP_KEY.matches(key, 0)) {
            SHOW_TAG_TOOLTIP_KEY.setDown(down);
        }
    }

    public static List<TagKey<Fluid>> getFluidTags(Fluid fluid){
        return fluid.defaultFluidState().getTags().toList();
    }

    @SuppressWarnings("unchecked")
    private static void addLine(List<?> tooltip, Component line, boolean isFabric){
        if(isFabric){
            ((List<Component>) tooltip).add(line);
        }
        else{
            ((List<Either<FormattedText, TooltipComponent>>) tooltip).add(Either.left(line));
        }
    }

    private static <T> void addTags(List<?> tooltip, List<TagKey<T>> tags, String title, boolean isFabric){
        if(!tags.isEmpty()){
            addLine(tooltip, Component.translatable("tooltip." + MOD_ID + "." + title).setStyle(TITLES), isFabric);
            for (TagKey<T> tag : tags) {
                addLine(tooltip, Component.literal("#" + tag.location()), isFabric);
            }
        }
    }

    public static void onMakeTooltip(List<?> tooltip, ItemStack stack, List<TagKey<Fluid>> fluidTags, boolean isFabric){

        if(TagTooltips.SHOW_TAG_TOOLTIP_KEY.isDown()){

            while (tooltip.size() > 1) {
                tooltip.removeLast();
            }

            List<TagKey<Item>> itemTags = new ArrayList<>(stack.getTags().toList());
            itemTags.sort(TAG_COMPARATOR);

            List<TagKey<Block>> blockTags = new ArrayList<>();
            List<TagKey<PoiType>> poiTags = new ArrayList<>();
            if(stack.getItem() instanceof BlockItem blockItem) {
                BlockState blockState = blockItem.getBlock().defaultBlockState();
                blockTags.addAll(blockState.getTags().toList());
                blockTags.sort(TAG_COMPARATOR);
                Optional<Holder<PoiType>> poiTypeHolder = PoiTypes.forState(blockState);
                if(poiTypeHolder.isPresent()){
                    poiTags.addAll(poiTypeHolder.get().tags().toList());
                    poiTags.sort(TAG_COMPARATOR);
                }
            }

            List<TagKey<EntityType<?>>> entityTags = new ArrayList<>();
            if(stack.getItem() instanceof SpawnEggItem spawnEgg){
                entityTags.addAll(spawnEgg.getType(stack).builtInRegistryHolder().tags().toList());
                entityTags.sort(TAG_COMPARATOR);
            }

            List<TagKey<Enchantment>> enchantmentTags = new ArrayList<>();
            List<Holder<Enchantment>> stackEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().stream().toList();
            if(stackEnchantments.size() == 1){
                enchantmentTags.addAll(stackEnchantments.getFirst().tags().toList());
                enchantmentTags.sort(TAG_COMPARATOR);
            }

            if (itemTags.isEmpty() && blockTags.isEmpty() && entityTags.isEmpty() && enchantmentTags.isEmpty()){
                addLine(tooltip, Component.translatable("tooltip." + MOD_ID + ".no_tags").setStyle(GREYED), isFabric);
            }
            else {
                addTags(tooltip, fluidTags, "fluid_tags", isFabric);
                addTags(tooltip, poiTags, "poi_type_tags", isFabric);
                addTags(tooltip, blockTags, "block_tags", isFabric);
                addTags(tooltip, entityTags, "entity_type_tags", isFabric);
                addTags(tooltip, enchantmentTags, "enchantment_tags", isFabric);
                if(stackEnchantments.size() > 1){
                    addLine(tooltip, Component.translatable("tooltip." + MOD_ID + ".enchantment_tags").setStyle(TITLES), isFabric);
                    addLine(tooltip, Component.translatable("tooltip." + MOD_ID + ".multiple_enchantments").setStyle(GREYED), isFabric);
                }
                addTags(tooltip, itemTags, "item_tags", isFabric);
            }

        }

    }

}
