package implementslegendkt.mod.vaultjp.network;

import implementslegendkt.mod.vaultjp.JewelPurpose;
import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity;
import io.netty.buffer.ByteBufOutputStream;
import iskallia.vault.container.StatisticsTabContainer;
import iskallia.vault.core.vault.stat.StatTotals;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public record UpdatePurposesPacket(BlockPos purposerPosition, List<JewelPurpose> purposes) {
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(purposerPosition);
        friendlyByteBuf.writeInt(purposes.size());
        for (var purpose:purposes) {
            friendlyByteBuf.writeNbt(JewelPurpose.writeNBT(purpose));
        }
    }

    public static UpdatePurposesPacket decode(FriendlyByteBuf friendlyByteBuf) {
        var pos = friendlyByteBuf.readBlockPos();
        var purposeCount = friendlyByteBuf.readInt();
        var purposes = new JewelPurpose[purposeCount];
        for (int i = 0; i < purposeCount; i++) {
            purposes[i] = JewelPurpose.readNBT(friendlyByteBuf.readNbt());
        }
        return new UpdatePurposesPacket(pos, Arrays.asList(purposes));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {

                if(sender.position().vectorTo(Vec3.atCenterOf(purposerPosition)).lengthSqr()<100){
                    if(sender.level.getBlockEntity(purposerPosition) instanceof JewelPurposerBlockEntity purposer){
                        purposer.purposes.clear();
                        purposer.purposes.addAll(purposes);
                        purposer.disposeBad();
                    }
                }


            }
        });
        context.setPacketHandled(true);
    }
}
