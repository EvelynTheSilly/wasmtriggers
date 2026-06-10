package name.wasmtriggers.hostFunctions

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import name.wasmtriggers.WasmTriggers

fun getLoggingFunctions(): Array<HostFunction> {
    val debug = HostFunction(
        "logging",
        "debug",
        FunctionType.of(
            listOf(ValType.I32, ValType.I32),
            listOf()
        )
    ) { instance: Instance, args: LongArray ->

        val offset = args[0].toInt()
        val len = args[1].toInt()

        val message = instance.memory().readString(offset, len)

        WasmTriggers.logger.debug(message)

        null
    }
    val info = HostFunction(
        "logging",
        "info",
        FunctionType.of(
            listOf(ValType.I32, ValType.I32),
            listOf()
        )
    ) { instance: Instance, args: LongArray ->

        val offset = args[0].toInt()
        val len = args[1].toInt()

        val message = instance.memory().readString(offset, len)

        WasmTriggers.logger.info(message)

        null
    }
    val warn = HostFunction(
        "logging",
        "warn",
        FunctionType.of(
            listOf(ValType.I32, ValType.I32),
            listOf()
        )
    ) { instance: Instance, args: LongArray ->

        val offset = args[0].toInt()
        val len = args[1].toInt()

        val message = instance.memory().readString(offset, len)

        WasmTriggers.logger.warn(message)

        null
    }

    val error = HostFunction(
        "logging",
        "error",
        FunctionType.of(
            listOf(ValType.I32, ValType.I32),
            listOf()
        )
    ) { instance: Instance, args: LongArray ->

        val offset = args[0].toInt()
        val len = args[1].toInt()

        val message = instance.memory().readString(offset, len)

        WasmTriggers.logger.error(message)

        null
    }


    return arrayOf(debug, info, warn, error)
}
