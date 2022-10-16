package dev.dnbln.klanx.syn.api

import dev.dnbln.klanx.syn.api.exceptions.ReachedEndException
import dev.dnbln.klanx.syn.impl.LexerImpl

interface Lexer {
    fun hasRemaining(): Boolean

    @Throws(ReachedEndException::class)
    fun peek(): Token

    @Throws(ReachedEndException::class)
    fun advance()

    companion object {
        operator fun invoke(src: TextSource): Lexer = LexerImpl(src)
    }
}
