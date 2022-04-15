package hunter.lang.generators

import hunter.lang.Ast

interface Generator {

    fun transform(ast: Ast) : Ast
    fun generate(ast: Ast) : String

}