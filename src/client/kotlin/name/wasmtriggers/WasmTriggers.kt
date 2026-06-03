package name.wasmtriggers

import name.wasmtriggers.event.KeyboardEvents
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
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
		registerCliFunctionality()

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

		KeyboardEvents.KEY_PRESS.register {handler, handle, action, event ->
			val event = event ?: return@register
            val keyName = GLFW.glfwGetKeyName(event.key, event.scancode) ?: return@register

			val client = Minecraft.getInstance()
			val isInGui = client.screen != null

			if (isInGui) {
				return@register
			}

            for (module in modules){
				if (action == GLFW.GLFW_PRESS){
					module.runKeyPressHandler(keyName)
				}
				if (action == GLFW.GLFW_RELEASE){
					module.runKeyReleaseHandler(keyName)
				}
				if (action == GLFW.GLFW_REPEAT){
					module.runKeyHoldHandler(keyName)
				}
				module.runKeyboardInputHandler(action, keyName)
			}
		}

		for (module in modules){
			module.runInitFunction()
		}
	}
}