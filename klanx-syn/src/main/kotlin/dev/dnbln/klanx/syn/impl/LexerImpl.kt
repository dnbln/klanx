package dev.dnbln.klanx.syn.impl

import dev.dnbln.klanx.syn.api.*
import dev.dnbln.klanx.syn.api.exceptions.ReachedEndException

internal class LexerImpl(private val src: TextSource) : Lexer {
    private var lastToken: Token? = null

    override fun hasRemaining(): Boolean = lastToken != null || src.hasRemaining()

    override fun peek(): Token {
        lastToken?.let { return@peek it }

        val token = lexNextToken()

        lastToken = token
        return token
    }

    override fun advance() {
        if (lastToken == null) {
            if (!src.hasRemaining())
                throw ReachedEndException()
            peek()
        }
        if (lastToken != null) lastToken = null
    }

    private fun lexNextToken(): Token = when (src.peek()) {
        ' ', '\t', '\n' -> lexWhitespace()
        '.' -> currentCharAndAdvance(Token.Punct::Dot)
        ',' -> currentCharAndAdvance(Token.Punct::Comma)
        '{' -> currentCharAndAdvance(Token.Punct::OpenBrace)
        '}' -> currentCharAndAdvance(Token.Punct::CloseBrace)
        '[' -> currentCharAndAdvance(Token.Punct::OpenBracket)
        ']' -> currentCharAndAdvance(Token.Punct::CloseBracket)
        '(' -> currentCharAndAdvance(Token.Punct::OpenParen)
        ')' -> currentCharAndAdvance(Token.Punct::CloseParen)
        '_', in 'a'..'z', in 'A'..'Z' -> lexIdentifier()
        in '0'..'9' -> lexNumber()

        else -> currentCharAndAdvance(Token::LexError)
    }

    private fun lexWhitespace(): Token.Whitespace {
        val startIndex = src.currentCharIndex()
        var hasNewline = false
        while (src.hasRemaining()) {
            when (src.peek()) {
                ' ', '\t' -> src.advance()
                '\n' -> {
                    src.advance()
                    hasNewline = true
                }

                else -> break
            }
        }
        val endIndex = src.currentCharIndex()

        return Token.Whitespace(TextRange(startIndex, endIndex), hasNewline)
    }

    private fun lexIdentifier(): Token {
        val startIndex = src.currentCharIndex()
        val id = StringBuilder()
        var empty = true

        while (src.hasRemaining()) {
            when (val c = src.peek()) {
                '_', in 'a'..'z', in 'A'..'Z' -> {
                    src.advance()
                    id.append(c)
                    empty = false
                }

                in '0'..'9' -> {
                    if (empty)
                        break

                    src.advance()

                    id.append(c)
                }

                else -> break
            }
        }

        val endIndex = src.currentCharIndex()

        val s = id.toString()

        val constructor: (TextRange, String) -> Token = when (s) {
            "fun" -> Token.Kw::Fun
            else -> Token::Id
        }

        return constructor(TextRange(startIndex, endIndex), s)
    }

    private fun lexNumber(): Token.Number {
        val startIndex = src.currentCharIndex()

        val b = StringBuilder()

        val f: (TextRange, String) -> Token.Number = when (val c = src.peek()) {
            '0' -> {
                b.append(c)
                src.advance()

                if (src.hasRemaining()) {
                    lexNumberStartingWith0(b)
                } else {
                    Token.Number::Oct
                }
            }

            in '1'..'9' -> {
                b.append(c)
                src.advance()

                lexDecNumber(b)

                Token.Number::Dec
            }

            else -> {
                error("Unknown number")
            }
        }

        val endIndex = src.currentCharIndex()

        return f(TextRange(startIndex, endIndex), b.toString())
    }

    private fun lexNumberStartingWith0(b: StringBuilder): (TextRange, String) -> Token.Number = when (val c = src.peek()) {
        'b' -> {
            b.append(c)

            src.advance()
            lexBinNumber(b)

            Token.Number::Bin
        }

        'x' -> {
            b.append(c)

            src.advance()
            lexHexNumber(b)

            Token.Number::Hex
        }

        in '0'..'7' -> {
            b.append(c)

            src.advance()
            lexOctNumber(b)

            Token.Number::Oct
        }

        else -> {
            Token.Number::Oct
        }
    }

    private fun lexBinNumber(b: StringBuilder) = lexNumberWithAlphabet(b, Alphabet.Set(setOf('0', '1')))
    private fun lexOctNumber(b: StringBuilder) = lexNumberWithAlphabet(b, Alphabet.Range('0'..'7'))
    private fun lexDecNumber(b: StringBuilder) = lexNumberWithAlphabet(b, Alphabet.Range('0'..'9'))
    private fun lexHexNumber(b: StringBuilder) = lexNumberWithAlphabet(
        b,
        Alphabet.Composite(Alphabet.Range('0'..'9'), Alphabet.Range('a'..'f'), Alphabet.Range('A'..'F'))
    )

    private fun lexNumberWithAlphabet(b: StringBuilder, alphabet: Alphabet) {
        while (src.hasRemaining()) {
            when (val c = src.peek()) {
                in alphabet -> {
                    b.append(c)
                    src.advance()
                }

                else -> {
                    return
                }
            }
        }
    }

    sealed class Alphabet {
        abstract operator fun contains(v: Char): Boolean

        class Set(private val set: kotlin.collections.Set<Char>) : Alphabet() {
            override operator fun contains(v: Char): Boolean = set.contains(v)
        }

        class Range(private val range: CharRange) : Alphabet() {
            override fun contains(v: Char): Boolean = range.contains(v)
        }

        class Composite(private vararg val alphabets: Alphabet) : Alphabet() {
            override fun contains(v: Char): Boolean = alphabets.any { it.contains(v) }
        }
    }

    private fun currentCharAndAdvance(f: (TextRange) -> Token): Token = f(src.currentCharRange()).also { src.advance() }
}
