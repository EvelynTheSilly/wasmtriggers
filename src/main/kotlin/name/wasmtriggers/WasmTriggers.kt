package name.wasmtriggers

import net.fabricmc.api.ClientModInitializer
import java.io.File;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.runtime.Instance;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object WasmTriggers : ClientModInitializer {
	const val MOD_ID = "wasmtriggers";
	val logger: Logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitializeClient() {
		logger.info("Initialising WasmTriggers")
	}
}