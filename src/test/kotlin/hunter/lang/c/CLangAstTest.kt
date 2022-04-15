package hunter.lang.c

import hunter.lang.Ast
import hunter.lang.Lexer
import hunter.lang.Parser
import hunter.lang.di
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import java.io.File

internal class CLangAstTest {

    private val lexer: Lexer by di.instance()
    private val parser: Parser by di.instance()

    @Test
    fun `Test`() {
        val tokens = lexer.lex(this.javaClass::class.java.getResource("/data/while-loop.hunt").readText())
        val expressions = parser.parseTokens(tokens)
        val hunterAst = Ast(expressions)
        val cAst = CLangTransformer().transform(hunterAst)

        hunterAst.print()
        cAst.print()

        assertTrue(false)
    }

}