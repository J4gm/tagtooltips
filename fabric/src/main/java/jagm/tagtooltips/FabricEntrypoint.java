package jagm.tagtooltips;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Iterator;

@Environment(EnvType.CLIENT)
public class FabricEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        KeyBindingHelper.registerKeyBinding(TagTooltips.SHOW_TAG_TOOLTIP_KEY);

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.beforeKeyPress(screen).register((screen1, key) -> TagTooltips.onKey(key, true));
            ScreenKeyboardEvents.beforeKeyRelease(screen).register((screen1, key) -> TagTooltips.onKey(key, false));
        });

        ItemTooltipCallback.EVENT.register((stack, context, tooltipFlag, tooltip) -> TagTooltips.onMakeTooltip(
                stack,
                () -> TagTooltips.clearTooltip(tooltip),
                tooltip::add,
                FabricEntrypoint::getFluid,
                entityType -> entityType.builtInRegistryHolder.tags())
        );

    }

    private static Fluid getFluid(ItemStack stack) {
        Storage<FluidVariant> fluidHandler = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (fluidHandler != null) {
            Iterator<StorageView<FluidVariant>> iterator = fluidHandler.nonEmptyIterator();
            if (iterator.hasNext()) {
                Fluid fluid = iterator.next().getResource().getFluid();
                return fluid.equals(Fluids.EMPTY) ? null : fluid;
            }
        }
        return null;
    }

}
