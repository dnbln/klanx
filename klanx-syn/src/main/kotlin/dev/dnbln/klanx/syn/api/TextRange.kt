package dev.dnbln.klanx.syn.api

import kotlin.math.max
import kotlin.math.min

data class TextRange(val startIndex: Int, val endIndex: Int) {
    init {
        checkRange()
    }

    private fun checkRange() {
        if (startIndex == -1 && endIndex == -1) return // allow to build Companion.EMPTY_RANGE

        if (startIndex < 0)
            throw IllegalArgumentException("startIndex < 0")

        if (endIndex < 0)
            throw IllegalArgumentException("endIndex < 0")

        if (endIndex <= startIndex)
            throw IllegalArgumentException("endIndex <= startIndex")
    }

    operator fun plus(other: TextRange) = union(other)
    operator fun times(other: TextRange) = intersection(other)

    fun union(other: TextRange): TextRange {
        if (this === EMPTY_RANGE) return other
        if (other === EMPTY_RANGE) return this

        return TextRange(min(startIndex, other.startIndex), max(endIndex, other.endIndex))
    }

    fun intersection(other: TextRange): TextRange {
        if (this === EMPTY_RANGE || other === EMPTY_RANGE) return EMPTY_RANGE

        if (startIndex > other.endIndex) return EMPTY_RANGE
        if (endIndex < other.startIndex) return EMPTY_RANGE

        return TextRange(max(startIndex, other.startIndex), min(endIndex, other.endIndex))
    }

    internal fun isValid() = startIndex in 0 until endIndex
    fun isValidWithin(context: TextRangeContext): Boolean {
        if (this === EMPTY_RANGE || !isValid()) return false

        return context.acceptsRange(startIndex, endIndex)
    }

    companion object {
        val EMPTY_RANGE = TextRange(-1, -1)
    }

    override fun toString(): String = "$startIndex..$endIndex"
}

fun String.substring(range: TextRange): String = substring(range.startIndex, range.endIndex)