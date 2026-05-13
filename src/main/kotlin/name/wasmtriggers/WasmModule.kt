package name.wasmtriggers

import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasi.WasiOptions
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wasi.WasiPreview1
import com.dylibso.chicory.wasm.InvalidException
import name.wasmtriggers.hostFunctons.getLoggingFunctions
import name.wasmtriggers.hostFunctons.logger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class WasmModule(val instance: Instance, val name: String) {
    val logger: Logger = LoggerFactory.getLogger("WasmModule")
    companion object{
        fun fromInstance(instance: Instance, name: String): WasmModule {
            return WasmModule(instance, name)
        }
        fun fromFile(file: File, name: String): WasmModule {
            val module = Parser.parse(file)

            val wasiOptions = WasiOptions.builder().build()!!
            val wasi = WasiPreview1.builder().withOptions(wasiOptions).build()

            val store = Store()

            for (func in wasi.toHostFunctions()){
                store.addFunction(func)
            }
            for (func in getLoggingFunctions()){
                logger.info("registering ${func.module()} ${func.name()}")
                store.addFunction(func)
            }

            val instance = store.instantiate(file.name, module)

            return fromInstance(instance, name)
        }
    }
    fun wasmFunction(name: String): ExportFunction? {
        return try {
            instance.export(name)
        }catch (e: InvalidException){
            logger.info("$e")
            null
        }
    }
    fun runInitFunction() {
        logger.info("initialising ${this.name}")
        val function = this.wasmFunction("init_handler")
        if (function == null) {
            logger.warn("module has no init_handler")
            return
        }
        function.apply()
    }
}