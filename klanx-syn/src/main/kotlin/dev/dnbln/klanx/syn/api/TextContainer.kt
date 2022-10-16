package dev.dnbln.klanx.syn.api

interface TextContainer {
    fun getTextAtRange(range: TextRange): String
}
