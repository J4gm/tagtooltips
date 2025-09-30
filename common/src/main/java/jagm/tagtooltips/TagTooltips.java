package jagm.tagtooltips;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class TagTooltips {

    public static final String MOD_ID = "tagtooltips";
    public static final KeyMapping SHOW_TAG_TOOLTIP_KEY = new KeyMapping("key." + MOD_ID + ".show_tag_tooltip", GLFW.GLFW_KEY_SEMICOLON, "key.categories.misc");

    private static final Style TITLES = Style.EMPTY.withColor(0xFFFFA0);
    private static final Style GREYED = Style.EMPTY.withColor(0xA0A0A0).withItalic(true);
    private static final Comparator<TagKey<?>> TAG_COMPARATOR = Comparator.comparing(key -> key.location().toString());

    public static void onKey(int key, boolean down) {
        if (InputConstants.getKey(key, 0) != InputConstants.UNKNOWN && SHOW_TAG_TOOLTIP_KEY.matches(key, 0)) {
            SHOW_TAG_TOOLTIP_KEY.setDown(down);
        }
    }

    public static void clearTooltip(List<?> tooltip) {
        while (tooltip.size() > 1) {
            tooltip.removeLast();
        }
    }

    private static <T> String tagToTranslationKey(TagKey<T> tag, String title) {
        String label = tag.location().toString();
        String[] split = label.split(":");
        if (split.length < 2) {
            return "tag." + title + ".minecraft." + label.replace("/", ".");
        } else {
            return "tag." + title + "." + split[0] + "." + split[1].replace("/", ".");
        }
    }

    private static <T> void addTags(Consumer<Component> tooltip, List<TagKey<T>> tags, String title) {
        if (!tags.isEmpty()) {
            tooltip.accept(Component.translatable("tooltip." + MOD_ID + "." + title).setStyle(TITLES));
            for (TagKey<T> tag : tags) {
                MutableComponent translation = Component.translatableWithFallback(tagToTranslationKey(tag, title), "");
                if (translation.getString().isEmpty()) {
                    tooltip.accept(Component.translatable("tooltip." + MOD_ID + ".tag", Component.literal(tag.location().toString()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.accept(Component.translatable("tooltip." + MOD_ID + ".tag_translatable", Component.literal(tag.location().toString()).withStyle(ChatFormatting.WHITE), translation).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    public static void onMakeTooltip(ItemStack stack, Runnable clearTooltip, Consumer<Component> tooltip, Function<ItemStack, Fluid> getFluid, Function<EntityType<?>, Stream<TagKey<EntityType<?>>>> tagsFromEntityType) {

        if (TagTooltips.SHOW_TAG_TOOLTIP_KEY.isDown()) {

            clearTooltip.run();

            List<TagKey<Item>> itemTags = new ArrayList<>(stack.getTags().toList());
            itemTags.sort(TAG_COMPARATOR);

            List<TagKey<Block>> blockTags = new ArrayList<>();
            List<TagKey<PoiType>> poiTags = new ArrayList<>();
            if (stack.getItem() instanceof BlockItem blockItem) {
                BlockState blockState = blockItem.getBlock().defaultBlockState();
                blockTags.addAll(blockState.getTags().toList());
                blockTags.sort(TAG_COMPARATOR);
                Optional<Holder<PoiType>> poiTypeHolder = PoiTypes.forState(blockState);
                if(poiTypeHolder.isPresent()){
                    poiTags.addAll(poiTypeHolder.get().tags().toList());
                    poiTags.sort(TAG_COMPARATOR);
                }
            }

            List<TagKey<Fluid>> fluidTags = new ArrayList<>();
            Fluid fluid = getFluid.apply(stack);
            if (fluid != null) {
                fluidTags.addAll(fluid.defaultFluidState().getTags().toList());
                fluidTags.sort(TAG_COMPARATOR);
            }

            List<TagKey<EntityType<?>>> entityTags = new ArrayList<>();
            if (stack.getItem() instanceof SpawnEggItem spawnEgg) {
                Level level = Minecraft.getInstance().level;
                if (level != null) {
                    entityTags.addAll(tagsFromEntityType.apply(spawnEgg.getType(level.registryAccess(), stack)).toList());
                    entityTags.sort(TAG_COMPARATOR);
                }
            }

            List<TagKey<Enchantment>> enchantmentTags = new ArrayList<>();
            List<Holder<Enchantment>> stackEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().stream().toList();
            if (stackEnchantments.size() == 1) {
                enchantmentTags.addAll(stackEnchantments.getFirst().tags().toList());
                enchantmentTags.sort(TAG_COMPARATOR);
            }

            if (itemTags.isEmpty() && blockTags.isEmpty() && entityTags.isEmpty() && enchantmentTags.isEmpty()) {
                tooltip.accept(Component.translatable("tooltip." + MOD_ID + ".no_tags").setStyle(GREYED));
            } else {
                addTags(tooltip, fluidTags, "fluid");
                addTags(tooltip, poiTags, "poi_type");
                addTags(tooltip, blockTags, "block");
                addTags(tooltip, entityTags, "entity_type");
                addTags(tooltip, enchantmentTags, "enchantment");
                if (stackEnchantments.size() > 1) {
                    tooltip.accept(Component.translatable("tooltip." + MOD_ID + ".enchantment").setStyle(TITLES));
                    tooltip.accept(Component.translatable("tooltip." + MOD_ID + ".multiple_enchantments").setStyle(GREYED));
                }
                addTags(tooltip, itemTags, "item");
            }

        }

    }

}
