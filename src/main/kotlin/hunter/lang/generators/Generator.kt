package hunter.lang.generators

import hunter.lang.Ast

interface Generator {

    fun generate(ast: Ast) : String

}