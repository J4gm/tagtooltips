package jagm.tagtooltips;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FabricEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        KeyBindingHelper.registerKeyBinding(TagTooltips.SHOW_TAG_TOOLTIP_KEY);

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.beforeKeyPress(screen).register(((screen1, key, scancode, modifiers) -> {
                TagTooltips.onKey(key, true);
            }));
            ScreenKeyboardEvents.beforeKeyRelease(screen).register(((screen1, key, scancode, modifiers) -> {
                TagTooltips.onKey(key, false);
            }));
        });

        ItemTooltipCallback.EVENT.register((stack, context, tooltipFlag, tooltip) -> {
            List<TagKey<Fluid>> fluidTags = new ArrayList<>();
            if(stack.getItem() instanceof BucketItem bucket){
                fluidTags = TagTooltips.getFluidTags(bucket.content);
            }
            TagTooltips.onMakeTooltip(tooltip, stack, fluidTags, true);
        });

    }

}
