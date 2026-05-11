package name.wasmtriggers

import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasi.WasiOptions
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wasi.WasiPreview1
import java.io.File

class WasmModule(val instance: Instance) {
    companion object{
        fun fromInstance(instance: Instance): WasmModule {
            return WasmModule(instance)
        }
        fun fromFile(file: File): WasmModule {
            val module = Parser.parse(file)

            val wasiOptions = WasiOptions.builder().build()!!
            val wasi = WasiPreview1.builder().withOptions(wasiOptions).build()

            val store = Store()
            store.addFunction()
            for (func in wasi.toHostFunctions()){
                store.addFunction(func)
            }

            val instance = store.instantiate(file.name, module)

            return fromInstance(instance)
        }
    }
    fun wasmFunction(name: String): ExportFunction {
        return instance.export(name)
    }
    fun runInitFunction(){
        val function = this.wasmFunction("module_init")
        function.apply()
    }
}