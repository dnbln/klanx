package dev.dnbln.klanx.syn.impl

import dev.dnbln.klanx.syn.api.TextRangeContext

internal class StringTextRangeContext(val s: String): TextRangeContext {
    override fun acceptsStartIndex(startIndex: Int): Boolean = startIndex in s.indices

    override fun acceptsEndIndex(endIndex: Int): Boolean = endIndex in (0..s.length)
}