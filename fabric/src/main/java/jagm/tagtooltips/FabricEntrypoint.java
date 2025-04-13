package jagm.tagtooltips;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;

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
            TagTooltips.onMakeTooltip(tooltip, stack, bucket -> bucket.content, entityType -> entityType.builtInRegistryHolder.tags(), true);
        });

    }

}
