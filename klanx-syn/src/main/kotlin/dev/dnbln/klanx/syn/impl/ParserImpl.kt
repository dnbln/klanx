package dev.dnbln.klanx.syn.impl

import dev.dnbln.klanx.syn.api.Lexer
import dev.dnbln.klanx.syn.api.parser.ParseProblemsHolder
import dev.dnbln.klanx.syn.api.parser.ParseTreeBuilder
import dev.dnbln.klanx.syn.api.parser.Parser
import dev.dnbln.klanx.syn.api.parser.parseTree.PtNodeInfo

internal class ParserImpl(private val lexer: Lexer) : Parser {
    private lateinit var problemsHolder: ParseProblemsHolder
    private lateinit var treeBuilder: ParseTreeBuilder

    private lateinit var parseEventStream: ParseEventStream

    override fun init(parseProblemsHolder: ParseProblemsHolder, parseTreeBuilder: ParseTreeBuilder) {
        problemsHolder = parseProblemsHolder
        treeBuilder = parseTreeBuilder

        parseEventStream = ParseEventStreamImpl()
    }

    override fun parse() {
        while (!lexer.hasRemaining()) {
            parseTop()
        }
    }

    fun parseTop() {
        if (!lexer.hasRemaining())
            return

        ctx(PtNodeInfo.Top) {}
    }

    fun ctx(treeNodeInfo: PtNodeInfo, f: (info: PtNodeInfo) -> Unit) {
        treeBuilder.begin(treeNodeInfo)

        f(treeNodeInfo)

        treeBuilder.end(treeNodeInfo)
    }
}
