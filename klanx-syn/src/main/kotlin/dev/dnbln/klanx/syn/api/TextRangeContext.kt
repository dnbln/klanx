package dev.dnbln.klanx.syn.api

interface TextRangeContext {
    fun acceptsStartIndex(startIndex: Int): Boolean
    fun acceptsEndIndex(endIndex: Int): Boolean

    fun acceptsRange(startIndex: Int, endIndex: Int) = acceptsStartIndex(startIndex) && acceptsEndIndex(endIndex)
}

