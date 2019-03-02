import cobalt.Compiler
import cobalt.codegen.ICodeGenerator
import cobalt.codegen.JavaScriptCodeGenerator
import cobalt.utilities.CompilerArgs
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    //Special calls for help/version
    if (args.count() == 2 && (args.contains("-h") || args.contains("--help"))) {
        printHelp()
        exitProcess(0)
    }
    if (args.count() == 2 && (args.contains("-v") || args.contains("--version"))) {
        printVersion()
        exitProcess(0)
    }

    //Parse compiler options
    var options:Map<String, Any>? = null
    try {
        options = CompilerArgs.parse(args)
    } catch (exception:Exception) {
        println(exception.message)
        printHelp()
        exitProcess(0)
    }

    //Proceed to compilation
    if (options != null) {

        try {

            val inputPath = options["input"] as String
            val outputPath = options["output"] as String
            val target = options["target"] as String
            val inputFile = File(inputPath)

            if (inputFile.exists()) {

                //Read input code
                val cobaltCode = inputFile.inputStream().readBytes().toString(Charsets.UTF_8)

                //Instantiate target code generator
                val codeGenerator: ICodeGenerator = when (target) {
                    "jvm" -> JavaScriptCodeGenerator()
                    else -> throw Exception("Unknown target '$target'.")
                }

                //Instantiate the compiler
                val compiler = Compiler(codeGenerator, options["debug"] as Boolean)

                //Compiler Cobalt to target code
                val targetCode = compiler.compile(cobaltCode)

                //Write to output file
                File(outputPath).writeText(targetCode)

                //Done without error!
                exitProcess(0)

            } else {
                throw Exception("Input file '$inputPath' does not exist.")
            }

        } catch (exception:Exception) {
            println(exception.message)
            exitProcess(1)
        }

    } else {
        throw Exception("This should not happen!")
    }

}

fun printHelp() {
    println("Available help options:")
    println("  -h/--help               Prints help.")
    println("  -v/--version            Prints compiler and Cobalt version.")
    println("Available compiler options:")
    println("  -i/--input  (required)  File path to read input Cobalt code from.")
    println("  -o/--output (required)  File path to write output code to.")
    println("  -t/--target (required)  Target to compile for (eg. 'jvm').")
    println("  --debug                 Activates debugging features.")
}

fun printVersion() {
    println("cobaltc 0.1.0 for Cobalt 0.1")
}