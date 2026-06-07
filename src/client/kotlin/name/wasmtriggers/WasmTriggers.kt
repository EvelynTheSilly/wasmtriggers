package name.wasmtriggers

import name.wasmtriggers.triggers.registerChatHandler
import name.wasmtriggers.triggers.registerGameMessageHandler
import name.wasmtriggers.triggers.registerKeyboardHandler
import name.wasmtriggers.triggers.registerTickHandler
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory


import name.wasmtriggers.cli.registerCliFunctionality

object WasmTriggers : ClientModInitializer {
    const val MOD_ID = "wasmtriggers"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)
    var modules: MutableList<WasmModule> = mutableListOf()

    override fun onInitializeClient() {
        logger.info("Initialising WasmTriggers")
        val baseDir = FabricLoader.getInstance().gameDir!!
        val wasmDir = baseDir.resolve("wasmtriggers")
        logger.info("grabbing modules from $wasmDir")

        modules = loadModulesFolder(wasmDir)
        registerCliFunctionality()

        registerChatHandler()
        registerGameMessageHandler()
        registerKeyboardHandler()
        registerTickHandler()

        for (module in modules) {
            module.runInitFunction()
        }
    }
}