package implementslegendkt.mod.vaultjp;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.logging.LogUtils;
import implementslegendkt.mod.vaultjp.network.Channel;
import implementslegendkt.mod.vaultjp.screen.JewelPurposerScreen;
import iskallia.vault.init.ModGameRules;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import static net.minecraft.world.level.GameRules.register;

@Mod("vaultjp")
public class Vaultjp {

    private static final Logger LOGGER = LogUtils.getLogger();


    public Vaultjp() {
        MinecraftForge.EVENT_BUS.register(this);

        if(FMLEnvironment.dist == Dist.CLIENT) {
            VaultJPClient.registerScreen();
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {


        @SubscribeEvent
        public static void setupCommon(FMLCommonSetupEvent event){
            Channel.registerPackets();
        }
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            JewelPurposerBlock.INSTANCE.setRegistryName("vaultjp","jewel_purpuser");
            blockRegistryEvent.getRegistry().register(JewelPurposerBlock.INSTANCE);
        }
        @SubscribeEvent
        public static void onBlockItemssRegistry(final RegistryEvent.Register<Item> blockRegistryEvent) {
            JewelPurposerBlock.ITEM.setRegistryName("vaultjp","jewel_purpuser");
            blockRegistryEvent.getRegistry().register(JewelPurposerBlock.ITEM);
        }
        @SubscribeEvent
        public static void onBlockEntitiesRegistry(final RegistryEvent.Register<BlockEntityType<?>> blockRegistryEvent) {
            JewelPurposerBlockEntity.TYPE.setRegistryName("vaultjp","jewel_purpuser_entity");
            blockRegistryEvent.getRegistry().register(JewelPurposerBlockEntity.TYPE);
        }
        @SubscribeEvent
        public static void onBlockMenuRegistry(final RegistryEvent.Register<MenuType<?>> blockRegistryEvent) {
            JewelPurposerContainer.MENU_TYPE.setRegistryName("vaultjp","jewel_purpuser_container");
            blockRegistryEvent.getRegistry().register(JewelPurposerContainer.MENU_TYPE);
        }
    }
}
