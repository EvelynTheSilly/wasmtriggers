package name.wasmtriggers

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

		ServerMessageEvents.CHAT_MESSAGE.register { message, player, bound ->
			val text = message.decoratedContent().string;
			for (module in modules){
				module.runChatMessageHandler(text)
			}
		 }

		for (module in modules){
			module.runInitFunction()
		}
	}
}