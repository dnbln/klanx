package dev.dnbln.klanx.syn.api

import dev.dnbln.klanx.syn.api.exceptions.ReachedEndException

interface TextSource {
    fun hasRemaining(): Boolean
    fun currentCharIndex(): Int

    @Throws(ReachedEndException::class)
    fun peek(): Char

    @Throws(ReachedEndException::class)
    fun advance()
}


fun TextSource.currentCharRange() = currentCharIndex().let { TextRange(it, it + 1) }