package dev.dnbln.klanx.syn.api.parser

import dev.dnbln.klanx.syn.api.Token
import dev.dnbln.klanx.syn.api.parser.parseTree.PtNodeInfo

interface ParseTreeBuilder {
    fun init()
    fun token(token: Token)
    fun begin(treeNodeKind: PtNodeInfo)
    fun end(treeNodeKind: PtNodeInfo)
    fun finish()
}
