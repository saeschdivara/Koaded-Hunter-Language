package hunter.lang

import hunter.lang.generators.Generator
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File

class Compiler(di: DI) {
    private val lexer: Lexer by di.instance()
    private val parser: Parser by di.instance()
    private val generator: Generator by di.instance()

    private var mainModule: Ast = Ast(emptyList())
    private var subModules = ArrayList<Ast>()

    fun compileModule(path: String) {

        val mainPath = File(path).parent
        val tokens = lexer.lexFile(path)

//        tokens.forEach {
//            println(it)
//        }

        val expressions = parser.parseTokens(tokens)
        mainModule = Ast(expressions)

//        println("")
//        println("Ast:")
//        mainModule.print()
        expressions.forEach {
            if (it is ImportExpression) {
                val submodulePath = resolveSubmodule(it.importPath)
                val submodule = compileSubModule("$mainPath/$submodulePath")
                subModules.add(submodule)

                val output = generator.generate(submodule)
                val filePath = "target/${submodulePath.replace(".hunt", ".c")}"
                File(filePath).writeText(output)
            }
        }

        var output = generator.generate(mainModule)

        output += "\n\n"

        output += """
            int main() {
                hunt();
                return 0;
            }
        """.trimIndent()

        File("target/test.c").writeText(output)
    }

    private fun resolveSubmodule(importPath: List<Token>) : String {
        return importPath.joinToString(separator = "/") {
            it.lexeme
        } + ".hunt"
    }

    private fun compileSubModule(path: String) : Ast {
        val tokens = lexer.lexFile(path)

//        tokens.forEach {
//            println(it)
//        }

        val expressions = parser.parseTokens(tokens)
        val ast = Ast(expressions)
//        ast.print()

        return ast
    }
}