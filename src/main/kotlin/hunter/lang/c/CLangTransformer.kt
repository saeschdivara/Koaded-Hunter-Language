package hunter.lang.c

import hunter.lang.*

// transforms hunter lang to c
class CLangTransformer {

    fun transform(ast: Ast) : CLangAst {
        val allExpressions = ArrayList<CLangExpr>()
        val includeExpressions = ArrayList<CLangExpr>()

        ast.expressions.forEach {
            val expr = transform(it)
            allExpressions.add(expr)
        }

        allExpressions.addAll(includeExpressions)
        return CLangAst(allExpressions)
    }

    private fun transform(expr: Expression) : CLangExpr {
        return when (expr) {
            is FunctionExpression -> transform(expr)
            is WhileExpression -> transform(expr)
            is VariableDefinitionExpression -> transform(expr)
            is VariableExpression -> transform(expr)
            is StringExpression -> transform(expr)
            is PrintExpression -> transform(expr)
            else -> CLangUnknownExpr()
        }
    }

    private fun transform(func: FunctionExpression) : CLangExpr {
        return CLangFunctionExpr(func.name.lexeme, func.body.map {
            transform(it)
        })
    }

    private fun transform(whileExpr: WhileExpression) : CLangExpr {
        return CLangWhileExpr(transform(whileExpr.condition), whileExpr.body.map {
            transform(it)
        })
    }

    private fun transform(printExpr: PrintExpression) : CLangExpr {
        return CLangFunctionCallExpr("printf", printExpr.parameters.map {
            transform(it)
        })
    }

    private fun transform(varDef: VariableDefinitionExpression) : CLangExpr {
        return CLangVariableDefinitionExpr(varDef.name.lexeme, transformValue(varDef.value))
    }

    private fun transform(varExpr: VariableExpression) : CLangExpr {
        return CLangVariableExpr(varExpr.value.lexeme)
    }

    private fun transform(str: StringExpression) : CLangExpr {
        return transformValue(str)
    }

    private fun transformValue(value: Expression) : CLangValueExpr {
        return when (value) {
            is IntExpression -> CLangValueExpr(rawValue = CLangRawValueExpr(CLangType.SIGNED_INT_16, value.value.lexeme))
            is StringExpression -> CLangValueExpr(rawValue = CLangRawValueExpr(CLangType.C_STRING, value.value.lexeme))
            else -> CLangValueExpr()
        }
    }

}