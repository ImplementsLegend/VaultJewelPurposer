package implementslegendkt.mod.vaultjp

import net.minecraft.world.level.GameRules.register
import com.mojang.logging.LogUtils
import implementslegendkt.mod.vaultjp.network.Channel.registerPackets
import iskallia.vault.init.ModGameRules
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.minecraft.world.level.GameRules
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
        if (FMLEnvironment.dist == Dist.CLIENT) VaultJPClient.registerScreen()
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
    object RegistryEvents {
        @SubscribeEvent
        @JvmStatic
        fun setupCommon(event: FMLCommonSetupEvent?) { registerPackets() }

        @SubscribeEvent
        @JvmStatic
        fun onBlocksRegistry(registryEvent: RegistryEvent.Register<Block?>) {
            registryEvent.registry.register(JewelPurposerBlock.apply { setRegistryName("vaultjp", "jewel_purpuser") })
        }

        @SubscribeEvent
        @JvmStatic
        fun onBlockItemssRegistry(registryEvent: RegistryEvent.Register<Item?>) {
            registryEvent.registry.register(JewelPurposerBlock.ITEM.apply { setRegistryName("vaultjp", "jewel_purpuser") })
        }

        @SubscribeEvent
        @JvmStatic
        fun onBlockEntitiesRegistry(registryEvent: RegistryEvent.Register<BlockEntityType<*>?>) {
            registryEvent.registry.register(JewelPurposerBlockEntity.TYPE.apply { setRegistryName("vaultjp", "jewel_purpuser_entity") })
        }

        @SubscribeEvent
        @JvmStatic
        fun onBlockMenuRegistry(registryEvent: RegistryEvent.Register<MenuType<*>?>) {
            registryEvent.registry.register(JewelPurposerContainer.MENU_TYPE.apply { setRegistryName("vaultjp", "jewel_purpuser_container") })
        }
    }

    companion object {
        private val LOGGER: Logger = LogUtils.getLogger()
        val KEEP_CONTENT_GAMERULE = register("vaultjpKeepsContent",GameRules.Category.DROPS, ModGameRules.booleanRule(false))
    }
}
