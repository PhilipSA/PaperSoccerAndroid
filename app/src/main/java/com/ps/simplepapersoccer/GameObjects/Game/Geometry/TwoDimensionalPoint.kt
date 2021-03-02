package com.ps.simplepapersoccer.gameObjects.game.geometry

data class TwoDimensionalPoint(var x: Int, var y: Int): Comparable<TwoDimensionalPoint> {

    fun set(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun equals(x: Int, y: Int): Boolean {
        return this.x == x && this.y == y
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val point = other as TwoDimensionalPoint

        if (x != point.x) return false
        return y == point.y
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString(): String {
        return "($x, $y) "
    }

    override fun compareTo(other: TwoDimensionalPoint): Int {
        val xDiff = x - other.x
        val yDiff = y - other.y
        return if (other.x == x && other.y == y) 0
        else if (yDiff > 0) 1
        else if (xDiff > 0 && other.y == y) 1
        else -1
    }
}