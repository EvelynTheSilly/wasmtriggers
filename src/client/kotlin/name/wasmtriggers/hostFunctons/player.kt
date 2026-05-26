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
            instance.memory().writeI32(args[0].toInt(), 0)
            instance.memory().writeI32(args[1].toInt(), 0)
            instance.memory().writeI32(args[2].toInt(), 0)
            instance.memory().writeI32(args[3].toInt(), 0)
        }
        else {
            instance.memory().writeI32(args[0].toInt(), 1)
            instance.memory().writeI32(args[1].toInt(), coordinates.x)
            instance.memory().writeI32(args[2].toInt(), coordinates.y)
            instance.memory().writeI32(args[3].toInt(), coordinates.z)
        }

        null
    }
    return arrayOf(getPlayerBlockPosition)
}
