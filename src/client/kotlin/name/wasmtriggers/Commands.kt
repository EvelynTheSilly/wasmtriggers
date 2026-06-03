package name.wasmtriggers

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import java.nio.file.Files
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

fun registerCliFunctionality() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
        val wt = ClientCommandManager.literal("wt")

        val unload = ClientCommandManager.literal("unload")
            .then(
                ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes { ctx -> unloadModule(ctx) }
            )

        val load = ClientCommandManager.literal("load")
            .then(
                ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes { ctx -> loadModule(ctx) }
            )

        val reload = ClientCommandManager.literal("reload")
            .then(
                ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes { ctx -> reloadModule(ctx) }
            )
            .executes { ctx -> reloadAllModules(ctx) }

        val list = ClientCommandManager.literal("list")
            .executes { ctx -> listModules(ctx) }

        dispatcher.register(wt.then(unload).then(load).then(reload).then(list))
    }
}

private fun unloadModule(ctx: CommandContext<FabricClientCommandSource>): Int {
    val name = StringArgumentType.getString(ctx, "name")
    val removed = WasmTriggers.modules.removeAll { it.name == name }
    return if (removed) {
        ctx.source.sendFeedback(Component.literal("Unloaded module: $name"))
        1
    } else {
        ctx.source.sendError(Component.literal("Module not found: $name"))
        0
    }
}

private fun loadModule(ctx: CommandContext<FabricClientCommandSource>): Int {
    val name = StringArgumentType.getString(ctx, "name")
    val wasmDir = FabricLoader.getInstance().gameDir!!.resolve("wasmtriggers")
    val file = wasmDir.resolve("$name.wasm").toFile()

    if (!file.exists()) {
        ctx.source.sendError(Component.literal("File not found: $name.wasm"))
        return 0
    }
    return try {
        val module = WasmModule.fromFile(file, name)
        WasmTriggers.modules.add(module)
        module.runInitFunction()
        ctx.source.sendFeedback(Component.literal("Loaded module: $name"))
        1
    } catch (e: Exception) {
        ctx.source.sendError(Component.literal("Failed to load $name: ${e.message}"))
        0
    }
}

private fun reloadModule(ctx: CommandContext<FabricClientCommandSource>): Int {
    val name = StringArgumentType.getString(ctx, "name")
    val wasmDir = FabricLoader.getInstance().gameDir!!.resolve("wasmtriggers")

    val wasUnloaded = WasmTriggers.modules.removeAll { it.name == name }
    if (wasUnloaded) {
        ctx.source.sendFeedback(Component.literal("Unloaded module: $name"))
    }

    val file = wasmDir.resolve("$name.wasm").toFile()
    if (!file.exists()) {
        ctx.source.sendError(Component.literal("File not found: $name.wasm"))
        return 0
    }
    return try {
        val module = WasmModule.fromFile(file, name)
        WasmTriggers.modules.add(module)
        module.runInitFunction()
        ctx.source.sendFeedback(Component.literal("Reloaded module: $name"))
        1
    } catch (e: Exception) {
        ctx.source.sendError(Component.literal("Failed to reload $name: ${e.message}"))
        0
    }
}

private fun reloadAllModules(ctx: CommandContext<FabricClientCommandSource>): Int {
    WasmTriggers.modules.clear()
    val wasmDir = FabricLoader.getInstance().gameDir!!.resolve("wasmtriggers")

    if (!Files.exists(wasmDir)) {
        ctx.source.sendError(Component.literal("Wasmtriggers directory not found"))
        return 0
    }

    Files.list(wasmDir).use { stream ->
        stream.filter { it.name.endsWith(".wasm") }.forEach {
            val moduleName = it.nameWithoutExtension
            try {
                val module = WasmModule.fromFile(it.toFile(), moduleName)
                WasmTriggers.modules.add(module)
                module.runInitFunction()
                ctx.source.sendFeedback(Component.literal("Loaded module: $moduleName"))
            } catch (e: Exception) {
                ctx.source.sendError(Component.literal("Failed to reload $moduleName: ${e.message}"))
            }
        }
    }
    return 1
}

private fun listModules(ctx: CommandContext<FabricClientCommandSource>): Int {
    val wasmDir = FabricLoader.getInstance().gameDir!!.resolve("wasmtriggers")
    if (!Files.exists(wasmDir)) {
        ctx.source.sendError(Component.literal("Wasmtriggers directory not found"))
        return 0
    }

    val loadedNames = WasmTriggers.modules.map { it.name }.toSet()

    ctx.source.sendFeedback(Component.literal("modules:"))

    Files.list(wasmDir).use { stream ->
        stream.filter { it.name.endsWith(".wasm") }
            .sorted()
            .forEach {
                val moduleName = it.nameWithoutExtension
                val isLoaded = moduleName in loadedNames
                val color = if (isLoaded) ChatFormatting.GREEN else ChatFormatting.RED
                val status = if (isLoaded) "loaded" else "unloaded"

                val component = Component.literal(moduleName)
                    .withStyle(Style.EMPTY
                        .withColor(color)
                        .withHoverEvent(HoverEvent.ShowText(Component.literal(status)))
                    )
                ctx.source.sendFeedback(component)
            }
    }
    return 1
}
