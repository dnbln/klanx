package dev.dnbln.klanx.syn.impl

import dev.dnbln.klanx.syn.api.TextRange
import dev.dnbln.klanx.syn.api.Token
import kotlin.reflect.KMutableProperty0

internal class ParseMarker {
    private var completed: CompletedParseMarker? = null
    private var abandoned: Boolean = false

    var parent: ParseMarker? = null
    var nextSibling: ParseMarker? = null
    var prevSibling: ParseMarker? = null
    var firstChild: ParseMarker? = null
    var lastChild: ParseMarker? = null

    var leaf: Token? = null

    fun siblingIter(direction: SiblingIterDirection = SiblingIterDirection.LTR) = SiblingIter(this, direction)

    fun <T> doWithPrevSibling(default: T? = null, f: (ParseMarker) -> T?): T? = doWith(::prevSibling, default, f)
    fun <T> doWithNextSibling(default: T? = null, f: (ParseMarker) -> T?): T? = doWith(::nextSibling, default, f)
    fun <T> doWithParent(default: T? = null, f: (ParseMarker) -> T?): T? = doWith(::parent, default, f)
    fun <T> doWithFirstChild(default: T? = null, f: (ParseMarker) -> T?): T? = doWith(::firstChild, default, f)
    fun <T> doWithLastChild(default: T? = null, f: (ParseMarker) -> T?): T? = doWith(::lastChild, default, f)

    fun <T> doWithPrevSibling(default: () -> T?, f: (ParseMarker) -> T?): T? = doWith(::prevSibling, default, f)
    fun <T> doWithNextSibling(default: () -> T?, f: (ParseMarker) -> T?): T? = doWith(::nextSibling, default, f)
    fun <T> doWithParent(default: () -> T?, f: (ParseMarker) -> T?): T? = doWith(::parent, default, f)
    fun <T> doWithFirstChild(default: () -> T?, f: (ParseMarker) -> T?): T? = doWith(::firstChild, default, f)
    fun <T> doWithLastChild(default: () -> T?, f: (ParseMarker) -> T?): T? = doWith(::lastChild, default, f)

    fun prevSiblingOr(default: ParseMarker?): ParseMarker? = propOr(::prevSibling, default)
    fun nextSiblingOr(default: ParseMarker?): ParseMarker? = propOr(::nextSibling, default)
    fun parentOr(default: ParseMarker?): ParseMarker? = propOr(::parent, default)
    fun firstChildOr(default: ParseMarker?): ParseMarker? = propOr(::firstChild, default)
    fun lastChildOr(default: ParseMarker?): ParseMarker? = propOr(::lastChild, default)

    private fun <T> doWith(prop: KMutableProperty0<ParseMarker?>, default: T? = null, f: (ParseMarker) -> T?): T? {
        val marker = prop.get() ?: return default

        return f(marker)
    }

    private fun <T> doWith(
        prop: KMutableProperty0<ParseMarker?>,
        default: () -> T? = { null },
        f: (ParseMarker) -> T?
    ): T? {
        val marker = prop.get() ?: return default()

        return f(marker)
    }

    private fun propOr(prop: KMutableProperty0<ParseMarker?>, default: ParseMarker?): ParseMarker? =
        prop.get() ?: default

    internal fun consistencyCheckSelfAndNeighbors(): Boolean {
        if (doWithPrevSibling(true) { it.nextSibling === this && it.parent === this.parent } == false) return false // prevSibling.nextSibling == this and share same parent
        if (doWithNextSibling(true) { it.prevSibling === this && it.parent === this.parent } == false) return false // nextSibling.prevSibling == this and share same parent
        if (doWithFirstChild(true) { it.parent === this } == false) return false // firstChild's parent is `this`
        if (doWithLastChild(true) { it.parent === this } == false) return false // lastChild's parent is `this`
        if (doWithFirstChild(true) { it.prevSibling == null } == false) return false // firstChild has no prevSibling
        if (doWithLastChild(true) { it.nextSibling == null } == false) return false // lastChild has no nextSibling

        return true
    }

    internal fun wholeTreeCheck(f: ParseMarker.() -> Boolean): Boolean {
        if (!f(this)) return false

        if (doWithFirstChild(true) { it.wholeTreeCheck(f) } == false) return false
        if (doWithNextSibling(true) { it.wholeTreeCheck(f) } == false) return false

        return true
    }

    internal fun wholeTreeConsistencyCheck(): Boolean = wholeTreeCheck { consistencyCheckSelfAndNeighbors() }
    internal fun wholeTreeComplete(): Boolean = wholeTreeCheck { completed != null || leaf != null }

    companion object {
        internal fun __internal__complete(parseMarker: ParseMarker, completedMarker: CompletedParseMarker) {
            if (parseMarker.abandoned) error("Tried to complete an abandoned marker")

            parseMarker.completed = completedMarker
        }

        internal fun __internal__abandon(parseMarker: ParseMarker) {
            if (parseMarker.completed != null) error("Tried to abandon a completed marker, it should be undone first")

            parseMarker.abandoned = true

            removeFromTree(parseMarker)

            parseMarker.firstChild = null
            parseMarker.lastChild = null
            parseMarker.prevSibling = null
            parseMarker.nextSibling = null
            parseMarker.parent = null
        }

        internal fun __internal__undocompletion(parseMarker: ParseMarker) {
            if (parseMarker.completed == null) error("Tried to undo an uncompleted marker")
            if (parseMarker.abandoned) error("Tried to undo an abandoned marker")

            parseMarker.completed = null
        }

        internal fun __internal__push_prevSibling(parseMarker: ParseMarker, prevSibling: ParseMarker) {
            parseMarker.doWithPrevSibling { it.nextSibling = prevSibling }
            prevSibling.prevSibling = parseMarker.prevSibling

            parseMarker.prevSibling = prevSibling
            prevSibling.nextSibling = parseMarker

            prevSibling.parent = parseMarker.parent

            parseMarker.doWithParent {
                if (it.firstChild === parseMarker)
                    it.firstChild = prevSibling
            }
        }

        internal fun __internal__push_nextSibling(parseMarker: ParseMarker, nextSibling: ParseMarker) {
            parseMarker.doWithNextSibling { it.prevSibling = nextSibling }
            nextSibling.nextSibling = parseMarker.nextSibling

            parseMarker.nextSibling = nextSibling
            nextSibling.prevSibling = parseMarker

            nextSibling.parent = parseMarker.parent

            parseMarker.doWithParent {
                if (it.lastChild === parseMarker)
                    it.lastChild = nextSibling
            }
        }

        internal fun __internal__insertParent(parseMarker: ParseMarker, parent: ParseMarker) {
            parseMarker.doWithPrevSibling { it.nextSibling = parent }

            parent.prevSibling = parseMarker.prevSibling

            parseMarker.doWithNextSibling { it.prevSibling = parent }

            parent.nextSibling = parseMarker.nextSibling

            parent.parent = parseMarker.parent
            parseMarker.parent = parent

            if (parent.firstChild == null) {
                parent.firstChild = parseMarker
                parent.lastChild = parseMarker
            } else {
                __internal__push_nextSibling(parent.lastChild!!, parseMarker)
            }
        }

        private fun removeFromTree(parseMarker: ParseMarker) {
            // move children one layer up
            parseMarker.doWithFirstChild { child ->
                child.siblingIter(SiblingIterDirection.LTR).forEach {
                    it.parent = parseMarker.parent
                }

                parseMarker.doWithPrevSibling { it.nextSibling = child }
                child.prevSibling = parseMarker.prevSibling

                parseMarker.doWithNextSibling { it.prevSibling = parseMarker.lastChild }
                parseMarker.doWithLastChild { lastChild ->
                    lastChild.nextSibling = parseMarker.nextSibling
                }
            }

            // unlink self
            parseMarker.doWithPrevSibling(default = {
                parseMarker.parent?.firstChild = parseMarker.nextSibling
            }) {
                it.nextSibling = parseMarker.nextSibling
            }

            parseMarker.doWithNextSibling(default = {
                parseMarker.parent?.lastChild = parseMarker.prevSibling
            }) {
                it.prevSibling = parseMarker.prevSibling
            }
        }

        internal fun newTree() = ParseMarker()
        internal fun newNextSibling(marker: ParseMarker): ParseMarker =
            ParseMarker().apply { __internal__push_nextSibling(marker, this) }

        internal fun newChild(marker: ParseMarker): ParseMarker =
            ParseMarker().apply {
                marker.doWithLastChild(default = {
                    marker.firstChild = this
                    marker.lastChild = this

                    this.parent = marker
                }) {
                    __internal__push_nextSibling(it, this)
                }
            }
    }
}

internal data class CompletedParseMarker(val nodeKind: NodeKind)

enum class NodeKind {

}


internal enum class SiblingIterDirection {
    LTR, RTL
}

internal class SiblingIter(node: ParseMarker, private val direction: SiblingIterDirection) : Iterator<ParseMarker> {
    var next: ParseMarker? = node

    override fun hasNext(): Boolean = next != null

    override fun next(): ParseMarker {
        val current = next ?: error("No next element")

        next = when (direction) {
            SiblingIterDirection.LTR -> current.nextSibling
            SiblingIterDirection.RTL -> current.prevSibling
        }

        return current
    }
}

internal class ParseMarkerTreeBuilder(private val root: ParseMarker) {
    fun child(f: ParseMarkerTreeBuilder.() -> Unit): ParseMarker {
        val marker = ParseMarker.newChild(root)

        val builder = ParseMarkerTreeBuilder(marker)
        f(builder)

        return marker
    }

    fun token(f: TokenBuilder.() -> Unit): ParseMarker {
        val builder = TokenBuilder()

        f(builder)

        val token = builder.build()

        val marker = ParseMarker.newChild(root).apply { this@apply.leaf = token }

        return marker
    }

    class TokenBuilder {
        var range = TextRange.EMPTY_RANGE
        lateinit var text: String
        var kind: ((TextRange) -> Token)? = null
        var complexKind: ((TextRange, String) -> Token)? = null

        fun ws(hasNewline: Boolean): (TextRange) -> Token =
            { textRange: TextRange -> Token.Whitespace(textRange, hasNewline) }

        internal fun build(): Token {
            if (complexKind != null) return complexKind!!(range, text)
            if (kind != null) return kind!!(range)

            error("No constructor passed")
        }
    }
}

internal fun buildTree(f: ParseMarkerTreeBuilder.() -> Unit): ParseMarker =
    ParseMarker.newTree().apply {
        f(ParseMarkerTreeBuilder(this))
    }
