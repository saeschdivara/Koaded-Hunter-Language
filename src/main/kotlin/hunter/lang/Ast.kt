package hunter.lang

open class Ast(open val expressions: List<Expression>) {

    fun print() {
        for (expression in expressions) {
            printExpression(expression, 0)
        }
    }

    private fun printExpression(expr: Expression, spaceLevel: Int) {
        if (expr is ExpressionWithBody) {
            printSpaceLevel(spaceLevel)
            println("${expr.javaClass.canonicalName} ${expr.getExtraInfo()}")
            printSpaceLevel(spaceLevel)
            println("Body:")

            for (bodyExpr in expr.body) {
                printExpression(bodyExpr, spaceLevel+2)
                println()
            }
        }
        else if (expr is ExpressionWithParameters) {
            printSpaceLevel(spaceLevel)
            println(expr.javaClass.canonicalName)
            printSpaceLevel(spaceLevel)
            println("Parameters:")

            for (bodyExpr in expr.parameters) {
                printExpression(bodyExpr, spaceLevel+2)
            }
        } else {
            printSpaceLevel(spaceLevel)
            println(expr)
        }
    }

    private fun printSpaceLevel(level: Int) {
        for (i in 0..level) {
            print(" ")
        }
    }

}