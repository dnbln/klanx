package dev.dnbln.klanx.syn.impl

import dev.dnbln.klanx.syn.api.Token
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ParseMarkerOpsTest {
    private fun assertRelations(
        marker: ParseMarker,
        prevSibling: ParseMarker? = null,
        nextSibling: ParseMarker? = null,
        parent: ParseMarker? = null,
        firstChild: ParseMarker? = null,
        lastChild: ParseMarker? = null
    ) {
        assertSame(marker.prevSibling, prevSibling)
        assertSame(marker.nextSibling, nextSibling)
        assertSame(marker.parent, parent)
        assertSame(marker.firstChild, firstChild)
        assertSame(marker.lastChild, lastChild)
    }

    @Test
    fun leavesWork() {
        lateinit var child1: ParseMarker
        lateinit var child2: ParseMarker
        lateinit var child3: ParseMarker
        lateinit var child4: ParseMarker

        val tree = buildTree {
            child1 = child {
                child2 = child {
                    child3 = token {
                        text = "Help"
                        complexKind = Token::Id
                    }

                    child4 = token {
                        text = " "
                        kind = ws(false)
                    }
                }
            }
        }

        assertNull(tree.leaf)
        assertNull(child1.leaf)
        assertNull(child2.leaf)
        assertNotNull(child3.leaf)
        assertNotNull(child4.leaf)
    }

    @Test
    fun consistencyCheck() {
        lateinit var child1: ParseMarker
        lateinit var child2: ParseMarker
        lateinit var child3: ParseMarker
        lateinit var child4: ParseMarker
        lateinit var child5: ParseMarker
        lateinit var child6: ParseMarker

        val tree = buildTree {
            child1 = child {
                child2 = child {
                    child3 = token {
                        text = "Help"
                        complexKind = Token::Id
                    }

                    child4 = token {
                        text = " "
                        kind = ws(false)
                    }
                }
            }

            child5 = child {
                child6 = token {
                    text = " "
                    kind = ws(false)
                }
            }
        }

        assertRelations(
            tree,
            firstChild = child1,
            lastChild = child5,
        )

        assertRelations(
            child1,
            nextSibling = child5,
            parent = tree,
            firstChild = child2,
            lastChild = child2,
        )

        assertRelations(
            child2,
            parent = child1,
            firstChild = child3,
            lastChild = child4,
        )

        assertRelations(
            child3,
            nextSibling = child4,
            parent = child2,
        )

        assertRelations(
            child4,
            prevSibling = child3,
            parent = child2,
        )

        assertRelations(
            child5,
            prevSibling = child1,
            parent = tree,
            firstChild = child6,
            lastChild = child6,
        )

        assertRelations(
            child6,
            parent = child5
        )


        assert(tree.wholeTreeConsistencyCheck())

        // attempt to link a new node to the left of child1, without changing tree.firstChild
        child1.prevSibling = buildTree { }
        child1.prevSibling!!.nextSibling = child1
        child1.prevSibling!!.parent = child1.parent

        assert(!tree.wholeTreeConsistencyCheck()) // tree.firstChild.prevSibling != null

        tree.firstChild = child1.prevSibling

        assert(tree.wholeTreeConsistencyCheck())

        // unlink the new subtree we created above
        tree.firstChild = child1

        child1.prevSibling!!.nextSibling = null
        child1.prevSibling!!.parent = null
        child1.prevSibling = null

        assert(tree.wholeTreeConsistencyCheck())
    }
}