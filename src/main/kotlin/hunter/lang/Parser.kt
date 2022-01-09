package hunter.lang

import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.collections.ArrayList
import kotlin.math.exp
import kotlin.system.exitProcess

interface Expression

interface ExpressionWithBody : Expression {
    val body: List<Expression>
}

interface ExpressionWithParameters : Expression {
    val parameters: List<Expression>
}

class EmptyExpression : Expression

data class FunctionExpression(val name: Token, override val body: List<Expression>) : ExpressionWithBody

data class PrintExpression(override val parameters: List<Expression>) : ExpressionWithParameters
data class StringExpression(val value: Token) : Expression
data class IntExpression(val value: Token) : Expression
data class VariableDefinitionExpression(val name: Token, val value: Expression) : Expression
data class VariableExpression(val value: Token) : Expression

class Parser {

    private var tokens: List<Token> = emptyList()
    private var bodies: Queue<ExpressionWithBody> = ArrayBlockingQueue(100)
    private var currentBodyLevel = 0
    private var current = 0

    fun parseTokens(tokens: List<Token>) : List<Expression> {
        this.tokens = tokens
        current = 0

        val expressions = ArrayList<Expression>()

        while (!isAtEnd()) {
            val token = advance()

            when (token.type) {
                TokenType.FUNCTION -> { expressions.add(parseFunction()) }
                else -> {}
            }
        }

        return expressions
    }

    private fun parseFunction() : FunctionExpression {

        val identifier = advance()
        if (identifier.type != TokenType.IDENTIFIER) {
            println("Function has no name")
            exitProcess(1)
        }

        val leftParen = advance()
        if (leftParen.type != TokenType.LeftParen) {
            println("Left paren missing after function")
            exitProcess(1)
        }

        val rightParen = advance()
        if (rightParen.type != TokenType.RightParen) {
            println("Right paren missing after function")
            exitProcess(1)
        }

        return FunctionExpression(identifier, parseBody())
    }

    private fun parseBody(): List<Expression> {
        var currentLevel = parseLevel()
        val expressions = ArrayList<Expression>()

        while (!isAtEnd() && currentLevel >= currentBodyLevel) {
            if (peek().type == TokenType.SpaceLevel) {
                currentLevel = parseLevel()
                continue
            }

            val token = advance()

            when (token.type) {
                TokenType.CONST -> { expressions.add(parseConst()) }
                TokenType.PRINT -> { expressions.add(parsePrint()) }
                else -> {}
            }
        }

        return expressions
    }

    private fun parsePrint() : PrintExpression {
        val leftParen = advance()
        if (leftParen.type != TokenType.LeftParen) {
            println("Left paren missing before function parameters")
            exitProcess(1)
        }

        val parameters = parseParameterList()

        val rightParen = advance()
        if (rightParen.type != TokenType.RightParen) {
            println("Right paren missing after function parameters")
            exitProcess(1)
        }

        return PrintExpression(parameters)
    }

    private fun parseParameterList() : List<Expression> {
        val parameters = ArrayList<Expression>()

        while (!isAtEnd() && peek().type != TokenType.RightParen) {
            val expr = parseSimpleExpression()
            parameters.add(expr)

            if (peek().type == TokenType.COMMA) {
                advance()
            }
        }

        return parameters
    }

    private fun parseConst() : VariableDefinitionExpression {
        val identifier = advance()
        if (identifier.type != TokenType.IDENTIFIER) {
            println("No variable name defined")
            exitProcess(1)
        }

        val equal = advance()
        if (equal.type != TokenType.EQUAL) {
            println("Equal after variable name missing")
            exitProcess(1)
        }

        return VariableDefinitionExpression(identifier, parseSimpleExpression())
    }

    private fun parseSimpleExpression() : Expression {

        val token = advance()

        return when (token.type) {
            TokenType.STRING -> { StringExpression(token) }
            TokenType.INT -> { IntExpression(token) }
            TokenType.IDENTIFIER -> { VariableExpression(token) }
            else -> { EmptyExpression() }
        }
    }

    private fun parseLevel(): Int {
        var level = 0

        while (peek().type == TokenType.SpaceLevel) {
            advance()
            level += 1
        }

        return level
    }

    private fun peek() : Token {
        return tokens[current]
    }

    private fun advance() : Token {
        return tokens[current++]
    }

    private fun isAtEnd(): Boolean {
        return current >= tokens.size
    }

}