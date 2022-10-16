package dev.dnbln.klanx.syn.api.parser

import dev.dnbln.klanx.syn.api.Lexer
import dev.dnbln.klanx.syn.impl.ParserImpl

interface Parser {
    fun init(
        parseProblemsHolder: ParseProblemsHolder,
        parseTreeBuilder: ParseTreeBuilder,
    )
    fun parse()

    companion object {
        fun parserWith(lexer: Lexer): Parser = ParserImpl(lexer)
    }
}