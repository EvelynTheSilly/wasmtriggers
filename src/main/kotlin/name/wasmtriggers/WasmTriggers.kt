package name.wasmtriggers

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.name

object WasmTriggers : ClientModInitializer {
	const val MOD_ID = "wasmtriggers"
	val logger: Logger = LoggerFactory.getLogger(MOD_ID)
	var modules: MutableList<WasmModule> = mutableListOf()

	override fun onInitializeClient() {
		logger.info("Initialising WasmTriggers")
		val baseDir = FabricLoader.getInstance().gameDir!!
		val wasmDir = baseDir.resolve("wasmtriggers")
		logger.info("grabbing modules from $wasmDir")

		Files.list(wasmDir).use { stream -> stream.forEach {
			if (!it.name.endsWith(".wasm")){
				logger.warn("file ${it.name} isn't a wasm file, despite being in the wasm directory")
				return@forEach
			}
			logger.info("importing ${it.name}")
			modules.add(WasmModule.fromFile(it.toFile()))
			logger.info("imported $it.name")
		} }

		for (module in modules){
			module.runInitFunction()
		}
	}
}