package name.wasmtriggers

import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasi.WasiOptions
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wasi.WasiPreview1
import com.dylibso.chicory.wasm.InvalidException
import com.dylibso.chicory.wasm.UnlinkableException
import com.dylibso.chicory.wasm.types.ExternalType
import name.wasmtriggers.hostFunctons.getChatFunctions
import name.wasmtriggers.hostFunctons.getLoggingFunctions
import name.wasmtriggers.hostFunctons.getPlayerFunctions
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.use

class WasmModule(val instance: Instance, val name: String) {
    companion object{
        fun fromInstance(instance: Instance, name: String): WasmModule {
            return WasmModule(instance, name)
        }
        fun fromFile(file: File, name: String): WasmModule {
            val module = Parser.parse(file)

            val wasiOptions = WasiOptions.builder().build()!!
            val wasi = WasiPreview1.builder().withOptions(wasiOptions).build()

            val store = Store()

            val hostFunctions = getLoggingFunctions() + getChatFunctions() + getPlayerFunctions()

            for (func in wasi.toHostFunctions()){
                store.addFunction(func)
            }
            for (func in hostFunctions){
                WasmTriggers.logger.info("registering ${func.module()} ${func.name()}")
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
            // WasmTriggers.logger.info("${this.name} has no function $name")
            null
        }
    }
    fun getAllHandlers(base: String): Array<ExportFunction> {
        val exports = instance.module().exportSection()
        return (0 until exports.exportCount())
            .map { exports.getExport(it) }
            .filter { it.exportType() == ExternalType.FUNCTION }
            .filter { it.name().startsWith("${base}__") || it.name() == base }
            .map { instance.export(it.name()) }
            .toTypedArray()
    }

    /**
     * allocates a string on the memory and returns a pointer and length
     * freeing string is the responsibility of the caller
     */
    fun allocateString(string: String): LongArray? {
        val alloc = wasmFunction("alloc")!!
        val len = string.toByteArray().size.toLong()
        val ptr = alloc.apply(len)[0]
        if (ptr == 0L) {
            // validate nonnull ptr
            return null
        }
        this.instance.memory().writeString(ptr.toInt(), string)
        return longArrayOf(ptr, len)
    }
    fun freeString(string: LongArray){
        val free = wasmFunction("dealloc")
        val ptr = string[0]
        val len = string[1]
        free?.apply(ptr, len)
    }
    fun runInitFunction() {
        WasmTriggers.logger.info("initialising ${this.name}")
        val functions = this.getAllHandlers("init_handler")
        for (function in functions){
            function.apply()
        }
    }
    fun runTickHandler(tick: Long) {
        val functions = this.getAllHandlers("on_tick")
        for (function in functions) {
            function.apply(tick)
        }
    }
    fun runChatMessageHandler(playerName: String, message: String){
        val handlers = getAllHandlers("chat_message_handler")
        val playerName = this.allocateString(playerName) ?: return
        val message = this.allocateString(message) ?: return
        for (handler in handlers) {
            handler.apply(
                *playerName,
                *message,
            )
        }
    }
    fun runServerMessageHandler(message: String){
        val handlers = getAllHandlers("server_message_handler")
        val message = this.allocateString(message) ?: return
        for (handler in handlers) {
            handler.apply(
                *message,
            )
        }
    }
    fun runKeyboardInputHandler(action: Int, key: String) {
        val handlers = getAllHandlers("on_keyboard_input")
        val keyAllocation = allocateString(key) ?: return
        for (handler in handlers) {
            handler.apply(action.toLong(), *keyAllocation)
        }
        freeString(keyAllocation)
    }
    fun runKeyPressHandler(key: String) {
        val handlers = getAllHandlers("on_keypress")
        val specificHandlers = getAllHandlers("on_keypress_${key}")
        val keyAllocation = allocateString(key) ?: return
        for (handler in handlers) {
            handler.apply(*keyAllocation)
        }
        for (handler in specificHandlers) {
            handler.apply()
        }
        freeString(keyAllocation)
    }
    fun runKeyReleaseHandler(key: String) {
        val handlers = getAllHandlers("on_keyrelease")
        val specificHandlers = getAllHandlers("on_keyrelease_${key}")
        val keyAllocation = allocateString(key) ?: return
        for (handler in handlers) {
            handler.apply(*keyAllocation)
        }
        for (handler in specificHandlers) {
            handler.apply()
        }
        freeString(keyAllocation)
    }
    fun runKeyHoldHandler(key: String) {
        val handlers = getAllHandlers("on_keyhold")
        val specificHandlers = getAllHandlers("on_keyhold_${key}")
        val keyAllocation = allocateString(key) ?: return
        for (handler in handlers) {
            handler.apply(*keyAllocation)
        }
        for (handler in specificHandlers) {
            handler.apply()
        }
        freeString(keyAllocation)
    }
}

fun loadModulesFolder(folder: Path): MutableList<WasmModule> {
    val modules = mutableListOf<WasmModule>()
    Files.list(folder).use { stream -> stream.forEach {
        if (!it.name.endsWith(".wasm")){
            WasmTriggers.logger.warn("file ${it.name} isn't a wasm file, despite being in the wasm directory")
            return@forEach
        }
        WasmTriggers.logger.info("importing ${it.name}")
        try {
            modules.add(WasmModule.fromFile(it.toFile(), it.nameWithoutExtension))
        } catch (e: UnlinkableException){
            WasmTriggers.logger.error("could not link ${e.message}")
            return@forEach
        }
        WasmTriggers.logger.info("imported ${it.name}")
    } }
    return modules
}
