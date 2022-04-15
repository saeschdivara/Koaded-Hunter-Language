package hunter.lang.c

import hunter.lang.Ast
import hunter.lang.Expression

interface CLangExpr : Expression
class CLangUnknownExpr : CLangExpr

interface CLangExprWithBody : CLangExpr {
    val body: List<CLangExpr>
}

data class CLangFunctionExpr(val name: String, override val body: List<CLangExpr>) : CLangExprWithBody

enum class CLangType {
    VOID,

    C_STRING,

    SIGNED_INT_8,
    SIGNED_INT_16,
    SIGNED_INT_32,
    SIGNED_INT_64,

    UNSIGNED_INT_8,
    UNSIGNED_INT_16,
    UNSIGNED_INT_32,
    UNSIGNED_INT_64,

    FLOAT,
    DOUBLE,
}

data class CLangRawValueExpr(val type: CLangType, val value: String) : CLangExpr
data class CLangVariableExpr(val variableName: String) : CLangExpr
data class CLangValueExpr(val rawValue: CLangRawValueExpr? = null, val variable: CLangVariableExpr? = null) : CLangExpr

data class CLangVariableDefinitionExpr(val variableName: String, val value: CLangValueExpr) : CLangExpr

data class CLangWhileExpr(val condition: CLangExpr, override val body: List<CLangExpr>) : CLangExprWithBody

data class CLangFunctionCallExpr(val functionName: String, val parameters: List<CLangExpr>) : CLangExpr

class CLangAst(override val expressions: List<CLangExpr>) : Ast(expressions) {
}