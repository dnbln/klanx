package dev.dnbln.klanx.syn.impl

import dev.dnbln.klanx.syn.api.TextRange
import dev.dnbln.klanx.syn.api.TextRangeContext
import dev.dnbln.klanx.syn.api.TextSourceContainer
import dev.dnbln.klanx.syn.api.exceptions.ReachedEndException
import dev.dnbln.klanx.syn.api.substring

internal class StringTextSourceContainer(private val s: String): TextSourceContainer, TextRangeContext by StringTextRangeContext(s) {
    private var ptr = 0

    override fun hasRemaining(): Boolean = ptr < s.length
    override fun currentCharIndex(): Int = ptr

    @Throws(ReachedEndException::class)
    override fun peek(): Char {
        if (!hasRemaining()) throw ReachedEndException()

        return s[ptr]
    }

    @Throws(ReachedEndException::class)
    override fun advance() {
        if (!hasRemaining()) throw ReachedEndException()

        ptr++
    }

    override fun getTextAtRange(range: TextRange): String {
        if (range === TextRange.EMPTY_RANGE) return ""

        if (!range.isValidWithin(this)) return ""

        return s.substring(range)
    }
}
