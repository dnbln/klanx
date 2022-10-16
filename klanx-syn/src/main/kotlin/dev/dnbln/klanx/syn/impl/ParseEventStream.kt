package dev.dnbln.klanx.syn.impl

internal interface ParseEventStream {
    fun init()

    fun begin(): ParseMarker
    fun complete(marker: ParseMarker): CompletedParseMarker
    fun abandon(marker: ParseMarker)
    fun precede(marker: ParseMarker): ParseMarker
    fun undoCompletion(marker: CompletedParseMarker): ParseMarker

    fun finish()

    companion object {
        fun createStream(): ParseEventStream = ParseEventStreamImpl()
    }
}

sealed class ParseEvent {

}

internal class ParseEventStreamImpl: ParseEventStream {
    val stream = mutableListOf<ParseEvent>()

    override fun init() {
        TODO("Not yet implemented")
    }

    override fun begin(): ParseMarker {
        TODO("Not yet implemented")
    }

    override fun complete(marker: ParseMarker): CompletedParseMarker {
        TODO("Not yet implemented")
    }

    override fun abandon(marker: ParseMarker) {
        TODO("Not yet implemented")
    }

    override fun precede(marker: ParseMarker): ParseMarker {
        TODO("Not yet implemented")
    }

    override fun undoCompletion(marker: CompletedParseMarker): ParseMarker {
        TODO("Not yet implemented")
    }

    override fun finish() {
        TODO("Not yet implemented")
    }
}
