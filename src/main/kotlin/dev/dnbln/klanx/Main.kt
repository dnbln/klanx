package dev.dnbln.klanx

import dev.dnbln.klanx.syn.api.Lexer
import dev.dnbln.klanx.syn.api.TextSourceContainer
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType

fun main(args: Array<String>) {
    val parser = ArgParser("klanx")
    val input by parser.argument(ArgType.String, fullName = "input-file", description = "Input file")
    parser.parse(args)

    println("Input file is $input")

    val container = TextSourceContainer("1 fun funx abxcvzxc 234 . 0123 0x123 0b123 078")
    val lx = Lexer(container)

    while (lx.hasRemaining()) {
        val token = lx.peek()

        println(token.textRepr(container))

        lx.advance()
    }
}
