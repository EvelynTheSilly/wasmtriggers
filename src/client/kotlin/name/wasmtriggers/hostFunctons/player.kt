package name.wasmtriggers.hostFunctons

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import name.wasmtriggers.WasmTriggers
import net.minecraft.client.Minecraft

fun getPlayerFunctions(): Array<HostFunction>{
    val getPlayerBlockPosition = HostFunction(
        "player_lib",
        "get_player_block_pos",
        FunctionType.of(
            listOf(ValType.I32, ValType.I32, ValType.I32, ValType.I32),
            listOf(),
        ),
    ) { instance, args ->
        val coordinates = Minecraft.getInstance().player?.blockPosition()
        if (coordinates == null){
            instance.memory().write(args[0].toInt(), 0.toByteArray())
            instance.memory().write(args[1].toInt(), 0.toByteArray())
            instance.memory().write(args[2].toInt(), 0.toByteArray())
            instance.memory().write(args[3].toInt(), 0.toByteArray())
        }
        else {
            WasmTriggers.logger.info("${coordinates.x}, ${coordinates.y}, ${coordinates.z}")
            instance.memory().write(args[0].toInt(), 1.toByteArray())
            instance.memory().write(args[1].toInt(), coordinates.x.toByteArray())
            instance.memory().write(args[2].toInt(), coordinates.y.toByteArray())
            instance.memory().write(args[3].toInt(), coordinates.z.toByteArray())
        }

        null
    }
    return arrayOf(getPlayerBlockPosition)
}

fun Int.toByteArray(): ByteArray = byteArrayOf(
    this.toByte(),
    (this shr 8).toByte(),
    (this shr 16).toByte(),
    (this shr 24).toByte()
)