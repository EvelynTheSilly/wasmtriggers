package name.wasmtriggers.triggers

import name.wasmtriggers.WasmTriggers
import name.wasmtriggers.event.KeyboardEvents
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

fun registerKeyboardHandler() {
    KeyboardEvents.KEY_PRESS.register {handler, handle, action, event ->
        val event = event ?: return@register
        val keyName = GLFW.glfwGetKeyName(event.key, event.scancode) ?: return@register

        val client = Minecraft.getInstance()
        val isInGui = client.screen != null

        if (isInGui) {
            return@register
        }

        for (module in WasmTriggers.modules){
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
}
