package com.ps.simplepapersoccer.Helpers

import java.util.ArrayList
import java.util.HashMap

class Tree<T>(val head: T) {

    private val leafs = ArrayList<Tree<T>>()

    var parent: Tree<T>? = null
        private set

    private var locate = HashMap<T, Tree<T>>()

    init {
        locate.put(head, this)
    }

    fun addLeaf(root: T, leaf: T) {
        if (locate.containsKey(root)) {
            locate[root]?.addLeaf(leaf)
        } else {
            addLeaf(root).addLeaf(leaf)
        }
    }

    fun addLeaf(leaf: T): Tree<T> {
        val t = Tree(leaf)
        leafs.add(t)
        t.parent = this
        t.locate = this.locate
        locate.put(leaf, t)
        return t
    }

    fun setAsParent(parentRoot: T): Tree<T> {
        val t = Tree(parentRoot)
        t.leafs.add(this)
        this.parent = t
        t.locate = this.locate
        t.locate.put(head, this)
        t.locate.put(parentRoot, t)
        return t
    }

    fun getTree(element: T): Tree<T>? {
        return locate[element]
    }

    fun getSuccessors(root: T): Collection<T> {
        val successors = ArrayList<T>()
        val tree = getTree(root)
        if (null != tree) {
            for (leaf in tree.leafs) {
                successors.add(leaf.head)
            }
        }
        return successors
    }

    val subTrees: Collection<Tree<T>>
        get() = leafs

    override fun toString(): String {
        return printTree(0)
    }

    private fun printTree(increment: Int): String {
        var s = ""
        var inc = ""
        for (i in 0..increment - 1) {
            inc = inc + " "
        }
        s = inc + head
        for (child in leafs) {
            s += "\n" + child.printTree(increment + indent)
        }
        return s
    }

    companion object {

        fun <T> getSuccessors(of: T, `in`: Collection<Tree<T>>): Collection<T> {
            for (tree in `in`) {
                if (tree.locate.containsKey(of)) {
                    return tree.getSuccessors(of)
                }
            }
            return ArrayList()
        }

        private val indent = 2
    }
}
