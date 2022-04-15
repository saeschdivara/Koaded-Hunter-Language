package hunter.lang.generators

import hunter.lang.*
import hunter.lang.c.CLangTransformer

class CLangGenerator : Generator {

    override fun transform(ast: Ast): Ast {
        val transformer = CLangTransformer()
        return transformer.transform(ast)
    }

    override fun generate(ast: Ast): String {
        return ""
    }

}