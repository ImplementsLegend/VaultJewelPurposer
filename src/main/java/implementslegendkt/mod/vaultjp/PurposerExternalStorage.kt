package implementslegendkt.mod.vaultjp

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.storage.DimensionDataStorage
import java.util.ArrayList
import java.util.UUID


class PurposerExternalStorage(val uuid:UUID, purposer:JewelPurposerBlockEntity):SavedData() {

    var deleted = false
    val inventory: JewelPurposerInventory = JewelPurposerInventory(purposer)
    var purposes: ArrayList<JewelPurpose> = ArrayList()
    override fun save(tag: CompoundTag): CompoundTag {
        tag.putUUID("uuid",uuid)

        val purposesTag = ListTag()
        for (purpose in purposes) {
            purposesTag.add(JewelPurpose.writeNBT(purpose))
        }
        tag.put("purposes", purposesTag)
        inventory.save(tag)
        return tag
    }

    override fun isDirty(): Boolean {
        return !deleted
    }

}
object PurposerExternalStorages {
    fun getOrCreateExternalStorage(storage:DimensionDataStorage,uuid:UUID, purposer:JewelPurposerBlockEntity): PurposerExternalStorage {
        return storage.get({
            tag->
            val storage = PurposerExternalStorage(uuid,purposer)
            storage.inventory.load(tag)
            for (purposeTag in tag.getList("purposes", CompoundTag.TAG_COMPOUND.toInt())) {
                storage.purposes.add(JewelPurpose.readNBT(purposeTag as CompoundTag))
            }
            storage
        },nameFor(uuid))?:PurposerExternalStorage(uuid,purposer)
    }
    fun saveExternalStorage(storage:DimensionDataStorage,data:PurposerExternalStorage){
        storage.set(nameFor(data.uuid),data)
    }

    fun nameFor(externalUUID: UUID?): String = "purposer_$externalUUID"
}