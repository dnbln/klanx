package dev.dnbln.klanx.syn.impl

internal interface ParseEventStream {
    fun init()

    fun begin(): PtNode
    fun complete(marker: PtNode, nodeKind: NodeKind)
    fun abandon(marker: PtNode)
    fun precede(marker: PtNode): PtNode

    fun finish()

    companion object {
        operator fun invoke(): ParseEventStream = ParseEventStreamImpl()
    }
}

data class ParseMarker(val index: Int)

sealed class ParseEvent {
    object Begin : ParseEvent()
    class Token(token: dev.dnbln.klanx.syn.api.Token) : ParseEvent()
    class End(nodeKind: NodeKind) : ParseEvent()

    object Ghost: ParseEvent()
}

internal class ParseEventStreamImpl : ParseEventStream {
    val stream = mutableListOf<ParseEvent>()

    override fun init() {}

    override fun begin(): PtNode {

    }

    override fun complete(marker: PtNode, nodeKind: NodeKind): CompletedParseMarker {
        TODO("Not yet implemented")
    }

    override fun abandon(marker: PtNode) {
        TODO("Not yet implemented")
    }

    override fun precede(marker: PtNode): PtNode {
        TODO("Not yet implemented")
    }

    override fun undoCompletion(marker: CompletedParseMarker): PtNode {
        TODO("Not yet implemented")
    }

    override fun finish() {
        TODO("Not yet implemented")
    }
}
