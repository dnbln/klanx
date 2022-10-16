package dev.dnbln.klanx.syn.impl

import dev.dnbln.klanx.syn.api.TextRange
import dev.dnbln.klanx.syn.api.Token
import kotlin.reflect.KMutableProperty0

internal class PtNode(val nodeKind: NodeKind) {
    var parent: PtNode? = null
    var nextSibling: PtNode? = null
    var prevSibling: PtNode? = null
    var firstChild: PtNode? = null
    var lastChild: PtNode? = null

    var leaf: Token? = null

    fun siblingIter(direction: SiblingIterDirection = SiblingIterDirection.LTR) = SiblingIter(this, direction)

    fun <T> doWithPrevSibling(default: T? = null, f: (PtNode) -> T?): T? = doWith(::prevSibling, default, f)
    fun <T> doWithNextSibling(default: T? = null, f: (PtNode) -> T?): T? = doWith(::nextSibling, default, f)
    fun <T> doWithParent(default: T? = null, f: (PtNode) -> T?): T? = doWith(::parent, default, f)
    fun <T> doWithFirstChild(default: T? = null, f: (PtNode) -> T?): T? = doWith(::firstChild, default, f)
    fun <T> doWithLastChild(default: T? = null, f: (PtNode) -> T?): T? = doWith(::lastChild, default, f)

    fun <T> doWithPrevSibling(default: () -> T?, f: (PtNode) -> T?): T? = doWith(::prevSibling, default, f)
    fun <T> doWithNextSibling(default: () -> T?, f: (PtNode) -> T?): T? = doWith(::nextSibling, default, f)
    fun <T> doWithParent(default: () -> T?, f: (PtNode) -> T?): T? = doWith(::parent, default, f)
    fun <T> doWithFirstChild(default: () -> T?, f: (PtNode) -> T?): T? = doWith(::firstChild, default, f)
    fun <T> doWithLastChild(default: () -> T?, f: (PtNode) -> T?): T? = doWith(::lastChild, default, f)

    fun prevSiblingOr(default: PtNode?): PtNode? = propOr(::prevSibling, default)
    fun nextSiblingOr(default: PtNode?): PtNode? = propOr(::nextSibling, default)
    fun parentOr(default: PtNode?): PtNode? = propOr(::parent, default)
    fun firstChildOr(default: PtNode?): PtNode? = propOr(::firstChild, default)
    fun lastChildOr(default: PtNode?): PtNode? = propOr(::lastChild, default)

    private fun <T> doWith(prop: KMutableProperty0<PtNode?>, default: T? = null, f: (PtNode) -> T?): T? {
        val marker = prop.get() ?: return default

        return f(marker)
    }

    private fun <T> doWith(
        prop: KMutableProperty0<PtNode?>,
        default: () -> T? = { null },
        f: (PtNode) -> T?
    ): T? {
        val marker = prop.get() ?: return default()

        return f(marker)
    }

    private fun propOr(prop: KMutableProperty0<PtNode?>, default: PtNode?): PtNode? =
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

    internal fun wholeTreeCheck(f: PtNode.() -> Boolean): Boolean {
        if (!f(this)) return false

        if (doWithFirstChild(true) { it.wholeTreeCheck(f) } == false) return false
        if (doWithNextSibling(true) { it.wholeTreeCheck(f) } == false) return false

        return true
    }

    internal fun wholeTreeConsistencyCheck(): Boolean = wholeTreeCheck { consistencyCheckSelfAndNeighbors() }
    internal fun wholeTreeComplete(): Boolean = wholeTreeCheck { completed != null || leaf != null }

    companion object {
        internal fun __internal__push_prevSibling(node: PtNode, prevSibling: PtNode) {
            node.doWithPrevSibling { it.nextSibling = prevSibling }
            prevSibling.prevSibling = node.prevSibling

            node.prevSibling = prevSibling
            prevSibling.nextSibling = node

            prevSibling.parent = node.parent

            node.doWithParent {
                if (it.firstChild === node)
                    it.firstChild = prevSibling
            }
        }

        internal fun __internal__push_nextSibling(node: PtNode, nextSibling: PtNode) {
            node.doWithNextSibling { it.prevSibling = nextSibling }
            nextSibling.nextSibling = node.nextSibling

            node.nextSibling = nextSibling
            nextSibling.prevSibling = node

            nextSibling.parent = node.parent

            node.doWithParent {
                if (it.lastChild === node)
                    it.lastChild = nextSibling
            }
        }

        internal fun __internal__insertParent(node: PtNode, parent: PtNode) {
            node.doWithPrevSibling { it.nextSibling = parent }

            parent.prevSibling = node.prevSibling

            node.doWithNextSibling { it.prevSibling = parent }

            parent.nextSibling = node.nextSibling

            parent.parent = node.parent
            node.parent = parent

            if (parent.firstChild == null) {
                parent.firstChild = node
                parent.lastChild = node

                node.prevSibling = null
                node.nextSibling = null
            } else {
                __internal__push_nextSibling(parent.lastChild!!, node)
            }
        }

        private fun removeFromTree(node: PtNode) {
            // move children one layer up
            node.doWithFirstChild { child ->
                child.siblingIter(SiblingIterDirection.LTR).forEach {
                    it.parent = node.parent
                }

                node.doWithPrevSibling { it.nextSibling = child }
                child.prevSibling = node.prevSibling

                node.doWithNextSibling { it.prevSibling = node.lastChild }
                node.doWithLastChild { lastChild ->
                    lastChild.nextSibling = node.nextSibling
                }
            }

            // unlink self
            node.doWithPrevSibling(default = {
                node.parent?.firstChild = node.nextSibling
            }) {
                it.nextSibling = node.nextSibling
            }

            node.doWithNextSibling(default = {
                node.parent?.lastChild = node.prevSibling
            }) {
                it.prevSibling = node.prevSibling
            }
        }

        internal fun newTree() = PtNode(NodeKind.Root)
        internal fun newNextSibling(node: PtNode, nodeKind: NodeKind): PtNode =
            PtNode(nodeKind).apply { __internal__push_nextSibling(node, this) }

        internal fun newChild(node: PtNode, nodeKind: NodeKind): PtNode =
            PtNode(nodeKind).apply {
                node.doWithLastChild(default = {
                    node.firstChild = this
                    node.lastChild = this

                    this.parent = node
                }) {
                    __internal__push_nextSibling(it, this)
                }
            }
    }
}


internal enum class SiblingIterDirection {
    LTR, RTL
}

internal class SiblingIter(node: PtNode, private val direction: SiblingIterDirection) : Iterator<PtNode> {
    var next: PtNode? = node

    override fun hasNext(): Boolean = next != null

    override fun next(): PtNode {
        val current = next ?: error("No next element")

        next = when (direction) {
            SiblingIterDirection.LTR -> current.nextSibling
            SiblingIterDirection.RTL -> current.prevSibling
        }

        return current
    }
}

internal class ParseTreeBuilder(private val root: PtNode) {
    fun child(nodeKind: NodeKind, f: ParseTreeBuilder.() -> Unit): PtNode {
        val node = PtNode.newChild(root, nodeKind)

        val builder = ParseTreeBuilder(node)
        f(builder)

        return node
    }

    fun token(nodeKind: NodeKind, f: TokenBuilder.() -> Unit): PtNode {
        val builder = TokenBuilder()

        f(builder)

        val token = builder.build()

        return PtNode.newChild(root, nodeKind).apply { leaf = token }
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

internal fun buildTree(f: ParseTreeBuilder.() -> Unit): PtNode =
    PtNode.newTree().apply {
        f(ParseTreeBuilder(this))
    }
