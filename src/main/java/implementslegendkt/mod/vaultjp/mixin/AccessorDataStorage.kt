package implementslegendkt.mod.vaultjp.mixin

import net.minecraft.world.level.storage.DimensionDataStorage
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Invoker
import java.io.File

@Mixin(DimensionDataStorage::class)
interface AccessorDataStorage {
    @Invoker fun callGetDataFile(p_78157_: String?): File?
}