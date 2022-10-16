package dev.dnbln.klanx.syn.api

sealed class Token(val range: TextRange, val text: String? = null) {
    abstract val tokenKindName: String
    fun getTokenText(textContainer: TextContainer): String = textContainer.getTextAtRange(range)
    fun textRepr(textContainer: TextContainer): String = """$tokenKindName "${getTokenText(textContainer)}" $range"""

    class Whitespace(range: TextRange, val hasNewline: Boolean) : Token(range) {
        override val tokenKindName: String = "WHITESPACE"
    }

    class Comment(range: TextRange) : Token(range) {
        override val tokenKindName: String = "COMMENT"
    }

    open class Id(range: TextRange, text: String) : Token(range, text) {
        override val tokenKindName: String = "ID"
    }

    class LexError(range: TextRange) : Token(range) {
        override val tokenKindName: String = "LEXERR"
    }

    sealed class Kw(range: TextRange, text: String) : Token(range, text) {
        class Fun(range: TextRange, text: String) : Kw(range, text) {
            override val tokenKindName: String = "FUN_KW"
        }
    }

    sealed class SoftKw(range: TextRange, text: String) : Id(range, text)

    sealed class Punct(range: TextRange) : Token(range) {
        class Dot(range: TextRange) : Punct(range) {
            override val tokenKindName: String = "DOT"
        }

        class Comma(range: TextRange) : Punct(range) {
            override val tokenKindName: String = "COMMA"
        }

        class OpenBrace(range: TextRange) : Punct(range) {
            override val tokenKindName: String = "OPEN_BRACE"
        }

        class CloseBrace(range: TextRange) : Punct(range) {
            override val tokenKindName: String = "CLOSE_BRACE"
        }

        class OpenBracket(range: TextRange) : Punct(range) {
            override val tokenKindName: String = "OPEN_BRACKET"
        }

        class CloseBracket(range: TextRange) : Punct(range) {
            override val tokenKindName: String = "CLOSE_BRACKET"
        }


        class OpenParen(range: TextRange) : Punct(range) {
            override val tokenKindName: String = "OPEN_PAREN"
        }

        class CloseParen(range: TextRange) : Punct(range) {
            override val tokenKindName: String = "CLOSE_PAREN"
        }
    }

    sealed class Number(range: TextRange, val value: String) : Token(range, value) {
        class Dec(range: TextRange, value: String) : Number(range, value) {
            override val tokenKindName: String = "DEC"
        }

        class Oct(range: TextRange, value: String) : Number(range, value) {
            override val tokenKindName: String = "OCT"
        }

        class Hex(range: TextRange, value: String) : Number(range, value) {
            override val tokenKindName: String = "HEX"
        }

        class Bin(range: TextRange, value: String) : Number(range, value) {
            override val tokenKindName: String = "BIN"
        }
    }
}
