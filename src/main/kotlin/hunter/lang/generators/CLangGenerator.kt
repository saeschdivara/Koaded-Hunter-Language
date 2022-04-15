package hunter.lang.generators

import hunter.lang.*
import hunter.lang.c.CLangTransformer

class CLangGenerator : Generator {

    override fun transform(ast: Ast): Ast {
        val transformer = CLangTransformer()
        return transformer.transform(ast)
    }

    override fun generate(ast: Ast): String {
        return getStandardLibraryIncludes() + generate(ast.expressions)
    }

    private fun generate(exprList: List<Expression>) : String {
        return generateList(exprList, "\n")
    }

    private fun generate(expr: ImportExpression) : String {
        return "#include \"${expr.importPath.map { it.lexeme }.joinToString("/")}.c\""
    }

    private fun generate(expr: FunctionExpression) : String {
        return """int ${expr.name.lexeme}() {
            |    ${generate(expr.body)}
            |    return 0;
            |}
        """.trimMargin()
    }

    private fun generate(expr: FunctionCallExpression) : String {
        return "${expr.functionName.lexeme}(${generateList(expr.properties, ",")});"
    }

    private fun generate(expr: PrintExpression) : String {
        val placeholders = expr.parameters.joinToString("") { "%s" }
        var parameters = ""

        if (expr.parameters.isNotEmpty()) {
            parameters = "," + generateList(expr.parameters, ",")
        }

        return "printf(\"$placeholders\"$parameters);"
    }

    private fun generate(expr: IntExpression) : String {
        return expr.value.lexeme
    }

    private fun generate(expr: StringExpression) : String {
        return "\"${expr.value.lexeme}\""
    }

    private fun generate(expr: VariableExpression) : String {
        return expr.value.lexeme
    }

    private fun generate(expr: VariableDefinitionExpression) : String {
        return "int ${expr.name.lexeme} = ${generateSingleExpression(expr.value)};"
    }

    private fun generate(expr: OperationExpression) : String {
        return "${generateSingleExpression(expr.left)} ${expr.operator.lexeme} ${generateSingleExpression(expr.right)}"
    }

    private fun generate(expr: WhileExpression) : String {
        return """while (${generateSingleExpression(expr.condition)}) {
            |    ${generateList(expr.body, "\n")}
            |}
        """.trimMargin()
    }

    private fun generateList(exprList: List<Expression>, separator: String) : String {
        val generatedExpressions = exprList.map {
            generateSingleExpression(it)
        }

        return generatedExpressions.joinToString(separator)
    }

    private fun generateSingleExpression(expr: Expression) : String {
        return when (expr) {
            is FunctionExpression -> generate(expr)
            is ImportExpression -> generate(expr)
            is FunctionCallExpression -> generate(expr)
            is WhileExpression -> generate(expr)
            is PrintExpression -> generate(expr)
            is StringExpression -> generate(expr)
            is IntExpression -> generate(expr)
            is OperationExpression -> generate(expr)
            is VariableDefinitionExpression -> generate(expr)
            is VariableExpression -> generate(expr)
            else -> ""
        }
    }

    private fun getStandardLibraryIncludes() : String {
        return """
            #include <stdio.h>

        """.trimIndent()
    }

}