package hunter.lang

import org.kodein.di.DI
import org.kodein.di.bindSingleton

val di = DI {
    bindSingleton { Lexer() }
    bindSingleton { Parser() }
}