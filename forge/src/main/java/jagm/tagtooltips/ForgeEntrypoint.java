package jagm.tagtooltips;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;
import java.util.Optional;

@Mod(TagTooltips.MOD_ID)
public class ForgeEntrypoint {

    public ForgeEntrypoint(FMLJavaModLoadingContext context) {}

    @Mod.EventBusSubscriber(modid = TagTooltips.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientGameEventHandler {

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(TagTooltips.SHOW_TAG_TOOLTIP_KEY);
        }

        @SubscribeEvent
        public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
            TagTooltips.onKey(new KeyEvent(event.getKeyCode(), event.getScanCode(), event.getModifiers()), true);
        }

        @SubscribeEvent
        public static void onKeyReleased(ScreenEvent.KeyReleased.Pre event) {
            TagTooltips.onKey(new KeyEvent(event.getKeyCode(), event.getScanCode(), event.getModifiers()), false);
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
            Optional<IFluidHandler> fluidHandlerOptional = stack.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve();
            if (fluidHandlerOptional.isPresent()) {
                Fluid fluid = fluidHandlerOptional.get().getFluidInTank(0).getFluid();
                return fluid.equals(Fluids.EMPTY) ? null : fluid;
            } else if (stack.getItem() instanceof BucketItem bucket) {
                return bucket.getFluid().equals(Fluids.EMPTY) ? null : bucket.getFluid();
            }
            return null;
        }

    }

}
