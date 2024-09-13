package implementslegendkt.mod.vaultjp.network

import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

@JvmRecord
data class ApplyJewelsPacket(val purposerPosition: BlockPos, val jewels: IntArray) {
    fun encode(friendlyByteBuf: FriendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(purposerPosition)
        friendlyByteBuf.writeVarIntArray(jewels)
    }

    fun handle(contextSupplier: Supplier<NetworkEvent.Context>) {
        val context = contextSupplier.get()
        context.enqueueWork {
            val sender = context.sender
            if (sender != null) {
                if (sender.position().vectorTo(Vec3.atCenterOf(purposerPosition)).lengthSqr() < 100) {
                    (sender.level.getBlockEntity(purposerPosition) as? JewelPurposerBlockEntity)?.let {
                        it.applyJewels(jewels)
                    }
                }
            }
        }
        context.packetHandled = true
    }

    companion object {
        @JvmStatic
        fun decode(friendlyByteBuf: FriendlyByteBuf): ApplyJewelsPacket {
            val pos = friendlyByteBuf.readBlockPos()
            val jewels = friendlyByteBuf.readVarIntArray()
            return ApplyJewelsPacket(pos, jewels)
        }
    }
}
