package name.wasmtriggers.triggers

import name.wasmtriggers.WasmTriggers
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

fun registerChatHandler() {
    ClientReceiveMessageEvents.CHAT.register { component, message, profile, bound, instant ->
        val text = message?.decoratedContent()?.string;
        for (module in WasmTriggers.modules){
            if (text != null) {
                module.runChatMessageHandler(profile?.name.toString(), text)
            }
        }
    }
}
