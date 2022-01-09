package hunter.lang

import java.nio.file.Files
import java.nio.file.Paths

enum class TokenType {
    INVALID,

    // Special token types
    COMMENT, LineBreak, SPACE, SpaceLevel,

    // Single-character tokens.
    COMMA, DOT, LeftParen, RightParen,
    MINUS, PLUS,

    // One or two character tokens.
    EQUAL, BANG, BangEqual, EqualEqual,

    // Literals.
    IDENTIFIER, STRING, INT, FLOAT,

    // Keywords.
    IF, ELSE, WHILE,
    CONST, FUNCTION,
    PRINT,
    TRUE, FALSE,

    EOF,
    ;
}

data class Token(val type: TokenType, val lexeme: String, val fileName: String, val line: Int)

class Lexer {

    private var filePath: String = ""
    private var data: String = ""
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1

    fun lexFile(path: String): List<Token> {
        // init
        filePath = path
        start = 0
        current = 0
        line = 1
        data = Files.readAllLines(Paths.get(path)).joinToString("\n")

        val tokens = ArrayList<Token>()

        while (!this.isAtEnd()) {
            val token = scanToken()

            when (token.type) {
                TokenType.COMMENT -> {}
                TokenType.SPACE -> {}
                TokenType.LineBreak -> { line += 1 }
                else -> {
                    tokens.add(token)
                }
            }
        }

        return tokens
    }

    private fun scanToken(): Token {
        start = current
        val character = advance()

        val tokenType = when (character) {
            '(' -> TokenType.LeftParen
            ')' -> TokenType.RightParen
            ',' -> TokenType.COMMA
            '=' -> TokenType.EQUAL
            ' ' -> {
                if (peek() == ' ') {
                    advance()
                    TokenType.SpaceLevel
                } else {
                    TokenType.SPACE
                }
            }
            '\r' -> TokenType.SPACE
            '\n' -> TokenType.LineBreak
            '\t' -> TokenType.SpaceLevel
            '"' -> scanString()
            else -> {
                if (character.isDigit()) {
                    scanNumber()
                } else if (character.isJavaIdentifierStart()) {
                    scanIdentifier()
                } else {
                    TokenType.INVALID
                }
            }
        }

        return createToken(tokenType)
    }

    private fun scanNumber(): TokenType {

        while (peek().isDigit()) {
            advance()
        }

        return TokenType.INT
    }

    private fun scanString(): TokenType {

        while (peek() != '"' && !isAtEnd()) {
            advance()
        }

        // The closing "
        advance()

        return TokenType.STRING
    }

    private fun scanIdentifier(): TokenType {

        while (peek().isJavaIdentifierPart()) {
            advance()
        }

        val tokenString = data.substring(start, current)

        return when(tokenString) {
            "fun" -> TokenType.FUNCTION
            "print" -> TokenType.PRINT
            "if" -> TokenType.IF
            "else" -> TokenType.ELSE
            "const" -> TokenType.CONST
            else -> TokenType.IDENTIFIER
        }
    }

    private fun createToken(type: TokenType): Token {
        var rangeBeginning = start
        var rangeEnding = current

        if (type == TokenType.STRING) {
            rangeBeginning += 1
            rangeEnding -= 1
        }

        return Token(type, data.substring(rangeBeginning, rangeEnding), filePath, line)
    }

    private fun peek(): Char {
        return data[current]
    }

    private fun advance(): Char {
        return data[current++]
    }

    private fun isAtEnd(): Boolean {
        return current >= data.length
    }

}