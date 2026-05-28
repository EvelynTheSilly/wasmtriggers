package name.wasmtriggers.hostFunctons

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Memory
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import name.wasmtriggers.WasmTriggers
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import java.net.URI

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

        val memory = instance.memory()
        val message = readChatComponentsFromMemory(memory, offset, len) ?: return@HostFunction null

        WasmTriggers.logger.info("logging $message")

        Minecraft.getInstance().player?.displayClientMessage(
            message,
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

fun readChatComponentsFromMemory(memory: Memory, offset: Int, len: Int): Component? {
    val structSize = 9 * 4
    val accumulator = Component.empty()
    var current = 0
    while (current < len) {
        val currentPtr = offset + (current * structSize)

        val textPointer = memory.readI32(currentPtr)
        val textLen = memory.readI32(currentPtr + 4)
        val textColor = memory.readI32(currentPtr + 8)
        val hoverPointer = memory.readI32(currentPtr + 12)
        val hoverLen = memory.readI32(currentPtr + 16)
        val hoverColor = memory.readI32(currentPtr + 20)
        val clickAction = memory.readI32(currentPtr + 24)
        val clickPointer = memory.readI32(currentPtr + 28)
        val clickLen = memory.readI32(currentPtr + 32)

        // malformation detection
        if (textPointer == 0L || textLen == 0L) {
            WasmTriggers.logger.info("discarding malformed text $textPointer, $textLen")
            return null
        }

        val text = memory.readString(textPointer.toInt(), textLen.toInt())

        val component = Component.literal(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(textColor.toInt())))
        if (hoverPointer != 0L || hoverLen != 0L) {
            WasmTriggers.logger.info("no hover")
            val hoverText = memory.readString(hoverPointer.toInt(), hoverLen.toInt()) ?: return null
            component.withStyle(Style.EMPTY.withHoverEvent(HoverEvent.ShowText(Component.literal(hoverText).withColor(hoverColor.toInt()))))
        }

        if (clickAction != 0L || clickPointer != 0L || clickLen != 0L) {
            WasmTriggers.logger.info("no click")
            val clickText = memory.readString(clickPointer.toInt(), clickLen.toInt()) ?: return null

            if (clickAction == 1L) {
                component.withStyle(Style.EMPTY.withClickEvent(ClickEvent.RunCommand(clickText)))
            }
            if (clickAction == 2L) {
                component.withStyle(Style.EMPTY.withClickEvent(ClickEvent.SuggestCommand(clickText)))
            }
            if (clickAction == 3L) {
                component.withStyle(Style.EMPTY.withClickEvent(ClickEvent.OpenUrl(URI.create(clickText))))
            }
            if (clickAction == 4L) {
                component.withStyle(Style.EMPTY.withClickEvent(ClickEvent.CopyToClipboard(clickText)))
            }
        }
        accumulator.append(component)
        current++
    }
    return accumulator
}