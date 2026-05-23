package name.wasmtriggers

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
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

		ClientReceiveMessageEvents.CHAT.register { component, message, profile, bound, instant ->
			val text = message?.decoratedContent()?.string;
			for (module in modules){
				if (text != null) {
					module.runChatMessageHandler(profile?.name.toString(), text)
				}
			}
		}
		ClientReceiveMessageEvents.GAME.register { component, bool ->
			if (bool) {
				return@register
			}
			val text = component.string;
			for (module in modules){
				module.runServerMessageHandler(text)
			}
		}

		for (module in modules){
			module.runInitFunction()
		}
	}
}