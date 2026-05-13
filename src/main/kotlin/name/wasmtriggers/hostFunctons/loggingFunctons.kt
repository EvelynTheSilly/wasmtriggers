package name.wasmtriggers.hostFunctons

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("WasmTriggersLogging")


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

        logger.debug(message)

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

        logger.info(message)

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

        logger.warn(message)

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

        logger.error(message)

        null
    }


    return arrayOf(debug, info, warn, error)
}