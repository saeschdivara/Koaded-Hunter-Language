package hunter.lang

import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

interface Expression

interface ExpressionWithBody : Expression {
    val body: List<Expression>
    fun getExtraInfo(): String
}

interface ExpressionWithParameters : Expression {
    val parameters: List<Expression>
}

class EmptyExpression : Expression

data class ImportExpression(val importPath: List<Token>): Expression
data class OperationExpression(val left: Expression, val operator: Token, val right: Expression): Expression

data class FunctionExpression(val name: Token, override val body: List<Expression>) : ExpressionWithBody {
    override fun getExtraInfo(): String {
        return "[name=${name.lexeme}]"
    }
}

data class StructExpression(val name: Token, override val body: List<Expression>) : ExpressionWithBody {
    override fun getExtraInfo(): String {
        return "[name=${name.lexeme}]"
    }
}

data class WhileExpression(val condition: Expression, override val body: List<Expression>) : ExpressionWithBody {
    override fun getExtraInfo(): String {
        return "[condition: $condition]"
    }
}

data class IfExpression(val condition: Expression, override val body: List<Expression>) : ExpressionWithBody {
    override fun getExtraInfo(): String {
        return "[condition: $condition]"
    }
}

data class ElseExpression(override val body: List<Expression>) : ExpressionWithBody {
    override fun getExtraInfo(): String {
        return ""
    }
}

data class PrintExpression(override val parameters: List<Expression>) : ExpressionWithParameters
data class StringExpression(val value: Token) : Expression
data class IntExpression(val value: Token) : Expression
data class VariableDefinitionExpression(val isConst: Boolean, val name: Token, val value: Expression) : Expression
data class VariableAssignmentExpression(val name: Token, val value: Expression) : Expression
data class VariableExpression(val value: Token) : Expression

data class PropertyAssignmentExpression(val propertyName: Token, val value: Expression) : Expression
data class StructConstructionExpression(val structName: Token, val properties: List<PropertyAssignmentExpression>) : Expression
data class PropertyDeclarationExpression(val propertyName: Token, val type: Token) : Expression

class Parser {

    private var tokens: List<Token> = emptyList()
    private val bodyLevels = Stack<Int>()
    private var current = 0

    fun parseTokens(tokens: List<Token>) : List<Expression> {
        this.tokens = tokens
        current = 0
        bodyLevels.push(0)

        val expressions = ArrayList<Expression>()

        while (!isAtEnd()) {
            val token = advance()

            when (token.type) {
                TokenType.IMPORT -> { expressions.add(parseImport()) }
                TokenType.FUNCTION -> { expressions.add(parseFunction()) }
                TokenType.STRUCT -> { expressions.add(parseStruct()) }
                else -> {}
            }
        }

        return expressions
    }

    private fun parseImport() : ImportExpression {
        val importPath = ArrayList<Token>()

        while (peek().type == TokenType.IDENTIFIER || peek().type == TokenType.ColonColon) {
            if (peek().type == TokenType.ColonColon) {
                continue
            }

            val identifier = advance()
            importPath.add(identifier)
        }

        return ImportExpression(importPath)
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

    private fun parseStruct() : StructExpression {

        val identifier = advance()
        if (identifier.type != TokenType.IDENTIFIER) {
            println("Function has no name")
            exitProcess(1)
        }

        return StructExpression(identifier, parseBody())
    }

    private fun parseBody(): List<Expression> {
        var currentLevel = parseLevel()
        val expressions = ArrayList<Expression>()

        if (currentLevel >= bodyLevels.peek()) {
            bodyLevels.push(currentLevel)
        }

        while (!isAtEnd() && currentLevel >= bodyLevels.peek()) {
            if (peek().type == TokenType.SpaceLevel) {
                currentLevel = parseLevel()
                continue
            }

            val token = advance()

            when (token.type) {
                TokenType.LET -> { expressions.add(parseLet()) }
                TokenType.CONST -> { expressions.add(parseConst()) }
                TokenType.WHILE -> { expressions.add(parseWhile()) }
                TokenType.IF -> { expressions.add(parseIf()) }
                TokenType.ELSE -> { expressions.add(parseElse()) }
                TokenType.PRINT -> { expressions.add(parsePrint()) }
                TokenType.IDENTIFIER -> {
                    if (peek().type == TokenType.EQUAL) {
                        expressions.add(parseVariableAssignment(token))
                    } else if (peek().type == TokenType.COLON) {
                        advance()
                        expressions.add(PropertyDeclarationExpression(token, advance()))
                    }
                }
                else -> {}
            }

            if (isAtEnd() || peek().type != TokenType.SpaceLevel) {
                break
            }
        }

        bodyLevels.pop()

        return expressions
    }

    private fun parseWhile() : WhileExpression {
        return WhileExpression(parseSimpleExpression(listOf(TokenType.SpaceLevel)), parseBody())
    }

    private fun parseIf() : IfExpression {
        val condition = parseSimpleExpression(listOf(TokenType.THEN))

        val thenToken = advance()
        if (thenToken.type != TokenType.THEN) {
            println("Then missing after if condition")
            exitProcess(1)
        }

        return IfExpression(condition, parseBody())
    }

    private fun parseElse() : ElseExpression {
        return ElseExpression(parseBody())
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
            val expr = parseSimpleExpression(listOf(TokenType.COMMA, TokenType.RightParen))
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

        return VariableDefinitionExpression(true, identifier, parseSimpleExpression(listOf(TokenType.SpaceLevel)))
    }

    private fun parseLet() : VariableDefinitionExpression {
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

        return VariableDefinitionExpression(false, identifier, parseSimpleExpression(listOf(TokenType.SpaceLevel)))
    }

    private fun parseVariableAssignment(identifier: Token) : VariableAssignmentExpression {
        val equal = advance()
        if (equal.type != TokenType.EQUAL) {
            println("Equal after variable name missing")
            exitProcess(1)
        }

        return VariableAssignmentExpression(identifier, parseSimpleExpression(listOf(TokenType.SpaceLevel)))
    }

    private fun parseSimpleExpression(breakPoints: List<TokenType>) : Expression {

        val expressionTokens = ArrayList<Token>()
        while (!isAtEnd() && !breakPoints.contains(peek().type)) {
            expressionTokens.add(advance())
        }

        return parseSimpleExpression2(expressionTokens)
    }

    // todo: rename to something better
    private fun parseSimpleExpression2(expressionTokens: List<Token>) : Expression {
        if (expressionTokens.size == 1) {
            val token = expressionTokens[0]
            return when (token.type) {
                TokenType.STRING -> { StringExpression(token) }
                TokenType.INT -> { IntExpression(token) }
                TokenType.IDENTIFIER -> { VariableExpression(token) }
                else -> { EmptyExpression() }
            }
        }

        if (expressionTokens[0].type == TokenType.NEW) {
            return parseStructConstruction(expressionTokens.subList(1, expressionTokens.size))
        }

        return parseOperations(expressionTokens)
    }

    private fun parseStructConstruction(tokens: List<Token>) : StructConstructionExpression {
        var tokenCounter = 0
        val identifier = tokens[tokenCounter++]
        if (identifier.type != TokenType.IDENTIFIER) {
            println("Struct constructions needs to have first a struct name")
            exitProcess(1)
        }

        val leftParen = tokens[tokenCounter++]
        if (leftParen.type != TokenType.LeftParen) {
            println("( after struct identifier is missing")
            exitProcess(1)
        }

        return StructConstructionExpression(identifier, parsePropertyAssignments(tokens.subList(tokenCounter, tokens.size)))
    }

    private fun parsePropertyAssignments(tokens: List<Token>): List<PropertyAssignmentExpression> {
        val assignments = ArrayList<PropertyAssignmentExpression>()
        var tokenCounter = 0

        while (tokenCounter < tokens.size) {

            val identifier = tokens[tokenCounter++]
            if (identifier.type != TokenType.IDENTIFIER) {
                println("Property needs to have first a name")
                exitProcess(1)
            }

            val equal = tokens[tokenCounter++]
            if (equal.type != TokenType.EQUAL) {
                println("After the name always follows a =")
                exitProcess(1)
            }

            val valueTokens = ArrayList<Token>()
            while (tokens[tokenCounter].type != TokenType.COMMA && tokens[tokenCounter].type != TokenType.RightParen) {
                valueTokens.add(tokens[tokenCounter++])
            }

            // skip comma
            tokenCounter += 1

            val value = parseSimpleExpression2(valueTokens)

            assignments.add(PropertyAssignmentExpression(identifier, value))
        }

        return assignments
    }

    private fun parseOperations(tokens: List<Token>) : OperationExpression {
        var operation: OperationExpression? = null
        var tokenCounter = 0

        while (tokenCounter < tokens.size && !isOperator(tokens[tokenCounter])) {
            tokenCounter += 1
            if (isOperator(tokens[tokenCounter])) {
                if (tokenCounter > 0) {
                    operation = OperationExpression(
                        parseSimpleExpression2(tokens.subList(0, tokenCounter)),
                        tokens[tokenCounter],
                        parseSimpleExpression2(tokens.subList(tokenCounter+1, tokens.size))
                    )

                    break
                }
            }
        }

        if (operation == null) {
            println("No operator found")
            exitProcess(1)
        }

        return operation
    }

    private fun isOperator(token: Token) : Boolean {
        return when (token.type) {
            TokenType.GREATER -> true
            TokenType.GreaterEqual -> true
            TokenType.LOWER -> true
            TokenType.LowerEqual -> true
            TokenType.EqualEqual -> true
            TokenType.PLUS -> true
            TokenType.MINUS -> true
            TokenType.DOT -> true
            else -> false
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