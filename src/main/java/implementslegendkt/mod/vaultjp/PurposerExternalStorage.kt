package implementslegendkt.mod.vaultjp

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.storage.DimensionDataStorage
import java.util.ArrayList
import java.util.UUID


class PurposerExternalStorage(val uuid:UUID, val purposer:JewelPurposerBlockEntity):SavedData() {

    var deleted = false
    val inventory: JewelPurposerInventory = JewelPurposerInventory(purposer)
    var purposes: ArrayList<JewelPurpose> = ArrayList()
    override fun save(tag: CompoundTag) = tag.apply{
        putUUID("uuid",uuid)

        put("purposes", ListTag().apply { addAll(purposes.map { JewelPurpose.writeNBT(it) }) })
        inventory.save(this)
    }

    override fun isDirty() = super.isDirty() && !deleted
    fun loadFromTag(tag: CompoundTag){
        inventory.load(tag)
        purposes.addAll(
            tag.getList("purposes", CompoundTag.TAG_COMPOUND.toInt())
                .map { JewelPurpose.readNBT(it as CompoundTag) })
    }

}
object PurposerExternalStorages {
    fun getOrCreateExternalStorage(storage:DimensionDataStorage,uuid:UUID, purposer:JewelPurposerBlockEntity) =
        storage.get({
            tag->
            PurposerExternalStorage(uuid,purposer).apply { loadFromTag(tag) }
        },nameFor(uuid))?.tryTransfer(purposer,storage)?:PurposerExternalStorage(uuid,purposer)

    fun saveExternalStorage(storage:DimensionDataStorage,data:PurposerExternalStorage){
        data.isDirty=true
        storage.set(nameFor(data.uuid),data)
    }
    fun PurposerExternalStorage.tryTransfer(purposer: JewelPurposerBlockEntity, storage: DimensionDataStorage): PurposerExternalStorage? {
        return if(this.purposer!==purposer){
            val tag = CompoundTag()
            save(tag)
            val new = PurposerExternalStorage(uuid,purposer)
            new.loadFromTag(tag)
            saveExternalStorage(storage,new)
            new
        }else this
    }

    fun nameFor(externalUUID: UUID?): String = "purposer_$externalUUID"
}

