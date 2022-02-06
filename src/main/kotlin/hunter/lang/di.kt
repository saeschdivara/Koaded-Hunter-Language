package hunter.lang

import hunter.lang.generators.CLangGenerator
import org.kodein.di.DI
import org.kodein.di.bindSingleton

val di = DI {
    bindSingleton { Lexer() }
    bindSingleton { Parser() }
    bindSingleton { CLangGenerator() }
}