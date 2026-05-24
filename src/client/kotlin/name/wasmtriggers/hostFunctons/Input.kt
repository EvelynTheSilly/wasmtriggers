package name.wasmtriggers.hostFunctons

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

fun getInputFunctions(): Array<HostFunction>{
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

        val window = Minecraft.getInstance().window.handle()

        null
    }

    return arrayOf(showChat)
}