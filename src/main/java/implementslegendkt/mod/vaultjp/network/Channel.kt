package implementslegendkt.mod.vaultjp.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public class Channel {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("vaultjp", "network"), ()->"vjp1.0", (s)->true, (s)->true
    );
    private static final AtomicInteger PACKET_ID = new AtomicInteger();

    public static void registerPackets(){

        CHANNEL.registerMessage(
                PACKET_ID.incrementAndGet(),
                ApplyJewelsPacket.class,
                ApplyJewelsPacket::encode,
                ApplyJewelsPacket::decode,
                ApplyJewelsPacket::handle
        );
        CHANNEL.registerMessage(
                PACKET_ID.incrementAndGet(),
                UpdatePurposesPacket.class,
                UpdatePurposesPacket::encode,
                UpdatePurposesPacket::decode,
                UpdatePurposesPacket::handle
        );
    }
}
