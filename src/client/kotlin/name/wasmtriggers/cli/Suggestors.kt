package name.wasmtriggers.cli

import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.loader.api.FabricLoader
import name.wasmtriggers.WasmTriggers
import java.nio.file.Files
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

private fun wasmFileNames(): List<String> {
    val wasmDir = FabricLoader.getInstance().gameDir!!.resolve("wasmtriggers")
    if (!Files.exists(wasmDir)) return emptyList()
    Files.list(wasmDir).use { stream ->
        return stream.filter { it.name.endsWith(".wasm") }
            .map { it.nameWithoutExtension }
            .toList()
    }
}

fun suggestLoadedModules(): SuggestionProvider<FabricClientCommandSource> = SuggestionProvider { _, builder ->
    WasmTriggers.modules.map { it.name }.forEach { builder.suggest(it) }
    builder.buildFuture()
}

fun suggestUnloadedModules(): SuggestionProvider<FabricClientCommandSource> = SuggestionProvider { _, builder ->
    val loaded = WasmTriggers.modules.map { it.name }.toSet()
    wasmFileNames().filter { it !in loaded }.forEach { builder.suggest(it) }
    builder.buildFuture()
}

fun suggestAllModules(): SuggestionProvider<FabricClientCommandSource> = SuggestionProvider { _, builder ->
    wasmFileNames().forEach { builder.suggest(it) }
    builder.buildFuture()
}
