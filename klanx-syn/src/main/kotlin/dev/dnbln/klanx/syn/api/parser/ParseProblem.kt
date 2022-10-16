package dev.dnbln.klanx.syn.api.parser

import dev.dnbln.klanx.syn.api.TextRange
import dev.dnbln.klanx.syn.api.Token

sealed class ParseProblem(location: TextRange) {
    class UnexpectedToken(private val token: Token): ParseProblem(token.range)
}
