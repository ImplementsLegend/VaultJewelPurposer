package implementslegendkt.mod.vaultjp.network

import implementslegendkt.mod.vaultjp.network.ApplyJewelsPacket
import implementslegendkt.mod.vaultjp.network.UpdatePurposesPacket
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.simple.SimpleChannel
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

object Channel {
    val CHANNEL: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation("vaultjp", "network"), { "vjp1.0" }, { s: String? -> true }, { s: String? -> true }
    )
    private val PACKET_ID = AtomicInteger()

    fun registerPackets() {
        CHANNEL.registerMessage(
            PACKET_ID.incrementAndGet(),
            ApplyJewelsPacket::class.java,
            { obj, friendlyByteBuf ->
                obj.encode(
                    friendlyByteBuf!!
                )
            },
            { obj -> ApplyJewelsPacket.decode(obj) },
            { obj, contextSupplier -> obj.handle(contextSupplier) }
        )
        CHANNEL.registerMessage(
            PACKET_ID.incrementAndGet(),
            UpdatePurposesPacket::class.java,
            { obj, friendlyByteBuf ->
                obj.encode(
                    friendlyByteBuf!!
                )
            },
            { obj -> UpdatePurposesPacket.decode(obj) },
            { obj, contextSupplier-> obj.handle(contextSupplier) }
        )
    }
}
