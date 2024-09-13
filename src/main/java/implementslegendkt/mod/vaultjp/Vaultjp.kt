package implementslegendkt.mod.vaultjp

import com.mojang.logging.LogUtils
import implementslegendkt.mod.vaultjp.network.Channel.registerPackets
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import org.slf4j.Logger

@Mod("vaultjp")
class Vaultjp {
    init {
        MinecraftForge.EVENT_BUS.register(this)

        if (FMLEnvironment.dist == Dist.CLIENT) {
            VaultJPClient.registerScreen()
        }
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
    object RegistryEvents {
        @SubscribeEvent
        @JvmStatic
        fun setupCommon(event: FMLCommonSetupEvent?) {
            registerPackets()
        }

        @SubscribeEvent
        @JvmStatic
        fun onBlocksRegistry(blockRegistryEvent: RegistryEvent.Register<Block?>) {
            JewelPurposerBlock.setRegistryName("vaultjp", "jewel_purpuser")
            blockRegistryEvent.registry.register(JewelPurposerBlock)
        }

        @SubscribeEvent
        @JvmStatic
        fun onBlockItemssRegistry(blockRegistryEvent: RegistryEvent.Register<Item?>) {
            JewelPurposerBlock.ITEM.setRegistryName("vaultjp", "jewel_purpuser")
            blockRegistryEvent.registry.register(JewelPurposerBlock.ITEM)
        }

        @SubscribeEvent
        @JvmStatic
        fun onBlockEntitiesRegistry(blockRegistryEvent: RegistryEvent.Register<BlockEntityType<*>?>) {
            JewelPurposerBlockEntity.TYPE.setRegistryName("vaultjp", "jewel_purpuser_entity")
            blockRegistryEvent.registry.register(JewelPurposerBlockEntity.TYPE)
        }

        @SubscribeEvent
        @JvmStatic
        fun onBlockMenuRegistry(blockRegistryEvent: RegistryEvent.Register<MenuType<*>?>) {
            JewelPurposerContainer.MENU_TYPE.setRegistryName("vaultjp", "jewel_purpuser_container")
            blockRegistryEvent.registry.register(JewelPurposerContainer.MENU_TYPE)
        }
    }

    companion object {
        private val LOGGER: Logger = LogUtils.getLogger()
    }
}
