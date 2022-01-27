package hunter.lang

import java.nio.file.Files
import java.nio.file.Paths

enum class TokenType {
    INVALID,

    // Special token types
    COMMENT, LineBreak, SPACE, SpaceLevel,

    // Single-character tokens.
    COMMA, DOT, LeftParen, RightParen,
    MINUS, PLUS, COLON,

    // One or two character tokens.
    EQUAL, BANG, BangEqual, EqualEqual,
    LOWER, GREATER, LowerEqual, GreaterEqual,
    ColonColon,

    // Literals.
    IDENTIFIER, STRING, INT, FLOAT,

    // Keywords.
    IMPORT,
    IF, THEN, ELSE, WHILE,
    CONST, LET, FUNCTION,
    PRINT,
    TRUE, FALSE,
    STRUCT, NEW,

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
                TokenType.COMMENT -> {
                    while (tokens.isNotEmpty() && tokens.last().type == TokenType.SpaceLevel) {
                        tokens.removeLast()
                    }
                }
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
            '+' -> TokenType.PLUS
            '-' -> TokenType.MINUS
            ':' -> {
                if (peek() == ':') {
                    advance()
                    TokenType.ColonColon
                } else {
                    TokenType.COLON
                }
            }
            '.' -> TokenType.DOT
            '#' -> {
                while (peek() != '\n') {
                    advance()
                }

                TokenType.COMMENT
            }
            '<' -> {
                if (peek() == '=') {
                    advance()
                    TokenType.LowerEqual
                } else {
                    TokenType.LOWER
                }
            }
            '>' -> {
                if (peek() == '=') {
                    advance()
                    TokenType.GreaterEqual
                } else {
                    TokenType.GREATER
                }
            }
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

        while (!isAtEnd() && peek().isDigit()) {
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
            "import" -> TokenType.IMPORT
            "fun" -> TokenType.FUNCTION
            "print" -> TokenType.PRINT
            "if" -> TokenType.IF
            "then" -> TokenType.THEN
            "else" -> TokenType.ELSE
            "while" -> TokenType.WHILE
            "struct" -> TokenType.STRUCT
            "new" -> TokenType.NEW
            "const" -> TokenType.CONST
            "let" -> TokenType.LET
            "eq" -> TokenType.EqualEqual
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