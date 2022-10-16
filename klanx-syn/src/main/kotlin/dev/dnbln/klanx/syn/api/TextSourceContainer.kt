package dev.dnbln.klanx.syn.api

import dev.dnbln.klanx.syn.impl.StringTextSourceContainer

interface TextSourceContainer: TextSource, TextContainer {
    companion object {
        operator fun invoke(s: String): TextSourceContainer = StringTextSourceContainer(s)
    }
}