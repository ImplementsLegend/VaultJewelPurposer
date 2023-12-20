package implementslegendkt.mod.vaultjp.network;

import implementslegendkt.mod.vaultjp.JewelPurpose;
import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public record ApplyJewelsPacket(BlockPos purposerPosition, int[] jewels) {
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(purposerPosition);
        friendlyByteBuf.writeVarIntArray(jewels);
    }

    public static ApplyJewelsPacket decode(FriendlyByteBuf friendlyByteBuf) {
        var pos = friendlyByteBuf.readBlockPos();
        var jewels = friendlyByteBuf.readVarIntArray();
        return new ApplyJewelsPacket(pos,jewels);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {

                if(sender.position().vectorTo(Vec3.atCenterOf(purposerPosition)).lengthSqr()<100){
                    if(sender.level.getBlockEntity(purposerPosition) instanceof JewelPurposerBlockEntity purposer){
                        purposer.applyJewels(jewels);
                    }
                }


            }
        });
        context.setPacketHandled(true);
    }
}
