package name.wasmtriggers.triggers

import name.wasmtriggers.WasmTriggers
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

fun registerGameMessageHandler() {
    ClientReceiveMessageEvents.GAME.register { component, bool ->
        if (bool) {
            return@register
        }
        val text = component.string;
        for (module in WasmTriggers.modules){
            module.runServerMessageHandler(text)
        }
    }
}
