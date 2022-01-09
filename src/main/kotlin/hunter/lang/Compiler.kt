package hunter.lang

import org.kodein.di.DI
import org.kodein.di.instance

class Compiler(di: DI) {
    private val lexer: Lexer by di.instance()
    private val parser: Parser by di.instance()

    fun compileModule(path: String) {

        val tokens = lexer.lexFile(path)

        tokens.forEach {
            println(it)
        }

        val expressions = parser.parseTokens(tokens)
        val ast = Ast(expressions)

        println("")
        println("Ast:")
        ast.print()
//        expressions.forEach {
//            println(it)
//        }
    }
}