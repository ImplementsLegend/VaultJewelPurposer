package implementslegendkt.mod.vaultjp.network

import implementslegendkt.mod.vaultjp.JewelPurpose
import implementslegendkt.mod.vaultjp.JewelPurposerBlockEntity
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import java.util.*
import java.util.function.Supplier

@JvmRecord
data class UpdatePurposesPacket(val purposerPosition: BlockPos, val purposes: List<JewelPurpose>) {
    fun encode(friendlyByteBuf: FriendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(purposerPosition)
        friendlyByteBuf.writeInt(purposes.size)
        for (purpose in purposes) {
            friendlyByteBuf.writeNbt(JewelPurpose.writeNBT(purpose))
        }
    }

    fun handle(contextSupplier: Supplier<NetworkEvent.Context>) {
        val context = contextSupplier.get()
        context.enqueueWork {
            if (context.direction == NetworkDirection.PLAY_TO_SERVER) {
                val sender = context.sender
                if (sender != null) {
                    if (sender.position().vectorTo(Vec3.atCenterOf(purposerPosition)).lengthSqr() < 100) {
                         (sender.level.getBlockEntity(purposerPosition) as? JewelPurposerBlockEntity)?.let {purposer->
                            purposer.purposes.clear()
                            purposer.purposes.addAll(purposes)
                            purposer.disposeBad()
                        }
                    }
                }
            }
            if (context.direction == NetworkDirection.PLAY_TO_CLIENT)
                (Minecraft.getInstance().level!!.getBlockEntity(purposerPosition) as? JewelPurposerBlockEntity)?.let { purposerBlockEntity->
                purposerBlockEntity.purposes.clear()
                purposerBlockEntity.purposes.addAll(purposes)
            }
        }
        context.packetHandled = true
    }

    companion object {
        @JvmStatic
        fun decode(friendlyByteBuf: FriendlyByteBuf): UpdatePurposesPacket {
            val pos = friendlyByteBuf.readBlockPos()
            val purposeCount = friendlyByteBuf.readInt()
            val purposes = Array(purposeCount){
                JewelPurpose.readNBT(friendlyByteBuf.readNbt()?:CompoundTag())
            }
            return UpdatePurposesPacket(pos, purposes.asList())
        }
    }
}
