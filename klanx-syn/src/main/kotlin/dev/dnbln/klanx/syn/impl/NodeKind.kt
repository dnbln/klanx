package dev.dnbln.klanx.syn.impl

enum class NodeKind {
    Root,

    // =======================
    // ======= Tokens ========
    // =======================

    // general
    TokWs,
    TokComment,
    TokId,


    // keywords
    TokFunKw,

    // punctuation
    TokDot,
    TokComma,
    TokOpenBrace,
    TokCloseBrace,
    TokOpenBracket,
    TokCloseBracket,
    TokOpenParen,
    TokCloseParen,

    // numbers
    TokDecNum,
    TokOctNum,
    TokHexNum,
    TokBinNum,

    // misc
    TokLexErr,
    ;

    fun isToken(): Boolean = this in TOKENS


    companion object {
        val TOKENS = setOf(
            TokWs,
            TokComment,
            TokId,
            TokFunKw,
            TokDot,
            TokComma,
            TokOpenBrace,
            TokCloseBrace,
            TokOpenBracket,
            TokCloseBracket,
            TokOpenParen,
            TokCloseParen,
            TokDecNum,
            TokOctNum,
            TokHexNum,
            TokBinNum,
            TokLexErr
        )
    }
}