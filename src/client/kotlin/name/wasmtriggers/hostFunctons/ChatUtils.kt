package name.wasmtriggers.hostFunctons

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

fun getChatFunctions(): Array<HostFunction>{
    val showChat = HostFunction(
        "chat_lib",
        "show_chat_message",
        FunctionType.of(
            listOf(ValType.I32, ValType.I32),
            listOf()
        ),
    ) { instance, args ->
        val offset = args[0].toInt()
        val len = args[1].toInt()

        val message = instance.memory().readString(offset, len)

        Minecraft.getInstance().player?.displayClientMessage(
            Component.literal(message),
            false
        )

        null
    }
    val sendChat = HostFunction(
        "chat_lib",
        "send_chat_message",
        FunctionType.of(
            listOf(ValType.I32, ValType.I32),
            listOf()
        ),
    ) { instance, args ->
        val offset = args[0].toInt()
        val len = args[1].toInt()

        val message = instance.memory().readString(offset, len)

        Minecraft.getInstance().player?.connection?.sendChat(message)

        null
    }
    val sendCommand = HostFunction(
        "chat_lib",
        "send_command",
        FunctionType.of(
            listOf(ValType.I32, ValType.I32),
            listOf()
        ),
    ) { instance, args ->
        val offset = args[0].toInt()
        val len = args[1].toInt()

        val message = instance.memory().readString(offset, len)

        Minecraft.getInstance().player?.connection?.sendCommand(message)

        null
    }
    return arrayOf(showChat, sendChat, sendCommand)
}