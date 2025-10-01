package jagm.tagtooltips;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.List;

@Mod(TagTooltips.MOD_ID)
public class NeoForgeEntrypoint {

    public NeoForgeEntrypoint(IEventBus eventBus) {}

    @EventBusSubscriber(modid = TagTooltips.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEventHandler {

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
            event.register(TagTooltips.SHOW_TAG_TOOLTIP_KEY);
        }

    }

    @EventBusSubscriber(modid = TagTooltips.MOD_ID, value = Dist.CLIENT)
    public static class ClientGameEventHandler {

        @SubscribeEvent
        public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event){
            TagTooltips.onKey(event.getKeyEvent(), true);
        }

        @SubscribeEvent
        public static void onKeyReleased(ScreenEvent.KeyReleased.Pre event){
            TagTooltips.onKey(event.getKeyEvent(), false);
        }

        @SubscribeEvent
        public static void onMakeTooltip(RenderTooltipEvent.GatherComponents event) {
            List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();
            TagTooltips.onMakeTooltip(
                    event.getItemStack(),
                    () -> TagTooltips.clearTooltip(tooltip),
                    line -> tooltip.add(Either.left(line)),
                    ClientGameEventHandler::getFluid,
                    EntityType::getTags
            );
        }

        private static Fluid getFluid(ItemStack stack) {
            ResourceHandler<FluidResource> fluidHandler = ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
            if (fluidHandler != null) {
                Fluid fluid = fluidHandler.getResource(0).getFluid();
                return fluid.equals(Fluids.EMPTY) ? null : fluid;
            }
            return null;
        }

    }

}
