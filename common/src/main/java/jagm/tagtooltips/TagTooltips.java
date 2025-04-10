package jagm.tagtooltips;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TagTooltips {

    public static final String MOD_ID = "tagtooltips";
    public static final String MOD_NAME = "Tag Tooltips";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final KeyMapping SHOW_TAG_TOOLTIP_KEY = new KeyMapping("key." + MOD_ID + ".show_tag_tooltip", GLFW.GLFW_KEY_SEMICOLON, "key.categories.misc");
    public static final Style TITLES = Style.EMPTY.withColor(0xFFFFA0).withBold(true);
    public static final Style GREYED = Style.EMPTY.withColor(0xA0A0A0);

    public static void onKey(int key, boolean down){
        if(InputConstants.getKey(key, 0) != InputConstants.UNKNOWN && SHOW_TAG_TOOLTIP_KEY.matches(key, 0)) {
            SHOW_TAG_TOOLTIP_KEY.setDown(down);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addLine(List<?> tooltip, Component line, boolean fabric){
        if(fabric){
            ((List<Component>) tooltip).add(line);
        }
        else{
            ((List<Either<FormattedText, TooltipComponent>>) tooltip).add(Either.left(line));
        }
    }

    public static void onMakeTooltip(List<?> tooltip, ItemStack stack, boolean fabric){

        if(TagTooltips.SHOW_TAG_TOOLTIP_KEY.isDown()){

            while (tooltip.size() > 1) {
                tooltip.removeLast();
            }

            Comparator<TagKey<?>> c = Comparator.comparing(tag -> tag.location().toString());
            List<TagKey<Item>> itemTags = new ArrayList<>(stack.getTags().toList());
            itemTags.sort(c);
            List<TagKey<Block>> blockTags = new ArrayList<>();
            if(stack.getItem() instanceof BlockItem blockItem) {
                blockTags = new ArrayList<>(blockItem.getBlock().defaultBlockState().getTags().toList());
                blockTags.sort(c);
            }

            if (itemTags.isEmpty() && blockTags.isEmpty()){
                addLine(tooltip, Component.translatable("tooltip.tagtooltips.no_tags").setStyle(TagTooltips.GREYED), fabric);
            }
            else {
                if(!itemTags.isEmpty()){
                    addLine(tooltip, Component.translatable("tooltip.tagtooltips.item_tags").setStyle(TagTooltips.TITLES), fabric);
                    for (TagKey<Item> itemTag : itemTags) {
                        addLine(tooltip, Component.literal("#" + itemTag.location().toString()), fabric);
                    }
                }
                if(!blockTags.isEmpty()){
                    addLine(tooltip, Component.translatable("tooltip.tagtooltips.block_tags").setStyle(TagTooltips.TITLES), fabric);
                    for (TagKey<Block> blockTag : blockTags) {
                        addLine(tooltip, Component.literal("#" + blockTag.location().toString()), fabric);
                    }
                }
            }

        }

    }

}
