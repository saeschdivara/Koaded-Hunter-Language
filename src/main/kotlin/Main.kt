import hunter.lang.Compiler
import hunter.lang.di

fun main(args: Array<String>) {
    // Try adding program arguments at Run/Debug configuration
    println("Program arguments: ${args.joinToString()}")

    val compiler = Compiler(di)
    compiler.compileModule(args[0])
}