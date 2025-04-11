package jagm.tagtooltips;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;

@Mod(TagTooltips.MOD_ID)
public class ForgeEntrypoint {

    public ForgeEntrypoint(FMLJavaModLoadingContext context){}

    @Mod.EventBusSubscriber(modid = TagTooltips.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEventHandler{

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
            event.register(TagTooltips.SHOW_TAG_TOOLTIP_KEY);
        }

    }

    @Mod.EventBusSubscriber(modid = TagTooltips.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
            List<TagKey<Fluid>> fluidTags = new ArrayList<>();
            if(event.getItemStack().getItem() instanceof BucketItem bucket){
                fluidTags = TagTooltips.getFluidTags(bucket.getFluid());
            }
            else if(event.getItemStack().getItem() instanceof IFluidTank fluidTank){
                fluidTags = TagTooltips.getFluidTags(fluidTank.getFluid().getFluid());
            }
            else if(event.getItemStack().getItem() instanceof BlockItem blockItem){
                if(blockItem instanceof IFluidTank fluidTank){
                    fluidTags = TagTooltips.getFluidTags(fluidTank.getFluid().getFluid());
                }
                if(blockItem instanceof IFluidBlock fluidBlock){
                    fluidTags = TagTooltips.getFluidTags(fluidBlock.getFluid());
                }
            }
            TagTooltips.onMakeTooltip(event.getTooltipElements(), event.getItemStack(), fluidTags, false);
        }

    }

}
