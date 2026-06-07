package name.wasmtriggers.triggers

import name.wasmtriggers.WasmTriggers
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

var tickCounter: Long = 0

fun registerTickHandler() {
    ClientTickEvents.START_CLIENT_TICK.register { _ ->
        tickCounter++
        for (module in WasmTriggers.modules) {
            module.runTickHandler(tickCounter)
        }
    }
}
