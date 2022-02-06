package hunter.lang.generators

import hunter.lang.*

class CLangGenerator : Generator {

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
        val placeholders = "%s"
        var parameters = ""

        if (expr.parameters.isNotEmpty()) {
            parameters = "," + generateList(expr.parameters, ",")
        }

        return "printf(\"$placeholders\"$parameters);"
    }

    private fun generate(expr: StringExpression) : String {
        return "\"${expr.value.lexeme}\""
    }

    private fun generateList(exprList: List<Expression>, separator: String) : String {
        val generatedExpressions = exprList.map {
            if (it is FunctionExpression)          generate(it)
            else if (it is ImportExpression)       generate(it)
            else if (it is FunctionCallExpression) generate(it)
            else if (it is PrintExpression)        generate(it)
            else if (it is StringExpression)       generate(it)
            else ""
        }

        return generatedExpressions.joinToString(separator)
    }

    private fun getStandardLibraryIncludes() : String {
        return """
            #include <stdio.h>

        """.trimIndent()
    }

}