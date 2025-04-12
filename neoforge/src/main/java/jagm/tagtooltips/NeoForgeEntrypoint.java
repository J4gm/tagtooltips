package jagm.tagtooltips;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@Mod(TagTooltips.MOD_ID)
public class NeoForgeEntrypoint {

    public NeoForgeEntrypoint(IEventBus eventBus){}

    @EventBusSubscriber(modid = TagTooltips.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientModEventHandler{

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
            event.register(TagTooltips.SHOW_TAG_TOOLTIP_KEY);
        }

    }

    @EventBusSubscriber(modid = TagTooltips.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class ClientGameEventHandler{

        @SubscribeEvent
        public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event){
            TagTooltips.onKey(event.getKeyCode(), true);
        }

        @SubscribeEvent
        public static void onKeyReleased(ScreenEvent.KeyReleased.Pre event){
            TagTooltips.onKey(event.getKeyCode(), false);
        }

        @SubscribeEvent
        public static void onMakeTooltip(RenderTooltipEvent.GatherComponents event){
            TagTooltips.onMakeTooltip(event.getTooltipElements(), event.getItemStack(), bucket -> bucket.content, false);
        }

    }

}
