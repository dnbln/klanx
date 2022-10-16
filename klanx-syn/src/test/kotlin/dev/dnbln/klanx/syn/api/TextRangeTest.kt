package dev.dnbln.klanx.syn.api

import dev.dnbln.klanx.syn.api.TextRange.Companion.EMPTY_RANGE
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TextRangeTest {
    @Test
    fun unionEmptyRange() {
        assertNotSame(EMPTY_RANGE, TextRange(1, 2).union(EMPTY_RANGE))
        assertNotSame(EMPTY_RANGE, EMPTY_RANGE.union(TextRange(1, 2)))
    }

    @Test
    fun union() {
        assertEquals(TextRange(1, 5), TextRange(1, 2).union(TextRange(3, 5)))
    }

    @Test
    fun properTextRange() {
        assertThrows(IllegalArgumentException::class.java) { TextRange(1, 0) }
    }
}