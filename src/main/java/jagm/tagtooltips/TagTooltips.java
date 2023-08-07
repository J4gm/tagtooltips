package jagm.tagtooltips;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent.GatherComponents;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(TagTooltips.MODID)
public class TagTooltips {

	public static final String MODID = "tagtooltips";

	private static final Style TAG_TYPE_TITLES = Style.EMPTY.withColor(0xFFFFA0).withBold(true);
	private static final Style GREYED = Style.EMPTY.withColor(0xA0A0A0);
	public static final String SHOW_TAG_TOOLTIP_KEY_NAME = "key.tagtooltips.show_tag_tooltip";
	public static final KeyMapping SHOW_TAG_TOOLTIP_KEY = new KeyMapping(SHOW_TAG_TOOLTIP_KEY_NAME, KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SEMICOLON,
			KeyMapping.CATEGORY_MISC);

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogUtils.getLogger();

	public TagTooltips() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(TagTooltips.class);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onMakeTooltip(GatherComponents event) {
		if (SHOW_TAG_TOOLTIP_KEY.isDown()) {
			List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();
			for (int i = tooltip.size() - 1; i > 0; i--) {
				tooltip.remove(i);
			}
			TagKeyComparator c = new TagKeyComparator();
			List<TagKey<Item>> itemTags = new LinkedList<TagKey<Item>>(event.getItemStack().getTags().toList());
			Collections.sort(itemTags, c);
			List<TagKey<Block>> blockTags = new LinkedList<TagKey<Block>>();
			if (event.getItemStack().getItem() instanceof BlockItem) {
				BlockItem blockItem = (BlockItem) event.getItemStack().getItem();
				blockTags = new LinkedList<TagKey<Block>>(blockItem.getBlock().defaultBlockState().getTags().toList());
				Collections.sort(blockTags, c);
			}
			if (blockTags.size() + itemTags.size() < 1) {
				tooltip.add(Either.left(Component.translatable("tooltip.tagtooltips.no_tags").setStyle(GREYED)));
			} else {
				if (itemTags.size() > 0) {
					tooltip.add(Either.left(Component.translatable("tooltip.tagtooltips.item_tags").setStyle(TAG_TYPE_TITLES)));
					for (TagKey<Item> itemTag : itemTags) {
						tooltip.add(Either.left(Component.literal("#" + itemTag.location().toString())));
					}
				}
				if (blockTags.size() > 0) {
					tooltip.add(Either.left(Component.translatable("tooltip.tagtooltips.block_tags").setStyle(TAG_TYPE_TITLES)));
					for (TagKey<Block> blockTag : blockTags) {
						tooltip.add(Either.left(Component.literal("#" + blockTag.location().toString())));
					}
				}
			}
		}
	}

	private class TagKeyComparator implements Comparator<TagKey<?>> {
		@Override
		public int compare(TagKey<?> o1, TagKey<?> o2) {
			return o1.location().toString().compareTo(o2.location().toString());
		}
	}

	@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ClientModBusEvents {
		@SubscribeEvent
		public static void onKeyRegister(RegisterKeyMappingsEvent event) {
			event.register(SHOW_TAG_TOOLTIP_KEY);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onKeyInput(InputEvent.Key event) {
		if(SHOW_TAG_TOOLTIP_KEY.isActiveAndMatches(InputConstants.getKey(event.getKey(), 0))) {
			SHOW_TAG_TOOLTIP_KEY.setDown(!(event.getAction() == InputConstants.RELEASE));
		}
	}

}
