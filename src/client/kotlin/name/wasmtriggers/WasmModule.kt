package name.wasmtriggers

import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasi.WasiOptions
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wasi.WasiPreview1
import com.dylibso.chicory.wasm.InvalidException
import com.dylibso.chicory.wasm.UnlinkableException
import name.wasmtriggers.hostFunctons.getChatFunctions
import name.wasmtriggers.hostFunctons.getLoggingFunctions
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

            val hostFunctions = getLoggingFunctions() + getChatFunctions()

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
            WasmTriggers.logger.info("$e")
            null
        }
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
    fun runInitFunction() {
        WasmTriggers.logger.info("initialising ${this.name}")
        val function = this.wasmFunction("init_handler")
        if (function == null) {
            WasmTriggers.logger.warn("module has no init_handler")
            return
        }
        function.apply()
    }
    fun runChatMessageHandler(playerName: String, message: String){
        val chatMessageHandler = wasmFunction("chat_message_handler") ?: return
        val playerName = this.allocateString(playerName) ?: return
        val message = this.allocateString(message) ?: return
        chatMessageHandler.apply(
            *playerName,
            *message,
        )
    }
    fun runServerMessageHandler(message: String){
        val serverMessageHandler = wasmFunction("server_message_handler") ?: return
        val message = this.allocateString(message) ?: return
        serverMessageHandler.apply(
            *message,
        )
    }
    fun runKeyboardInputHandler() {
        val keyHandler = wasmFunction("on_keyBoardInput") ?: return
        keyHandler.apply()
    }
    fun runKeyPressHandler(key: Int) {
        val keyHandler = wasmFunction("on_keypress") ?: return
        keyHandler.apply()
    }
    fun runKeyReleaseHandler() {
        val keyHandler = wasmFunction("on_keyrelease") ?: return
        keyHandler.apply()
    }
    fun runKeyHoldHandler() {
        val keyHandler = wasmFunction("on_keyHold") ?: return
        keyHandler.apply()
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
