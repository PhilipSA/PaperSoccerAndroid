package com.ps.simplepapersoccer.gameObjects.game.geometry

data class TwoDimensionalPoint(var x: Int, var y: Int) {
    fun toAndroidPoint() = android.graphics.Point(x, y)

    fun set(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun negate() {
        x = -x
        y = -y
    }

    fun offset(dx: Int, dy: Int) {
        x += dx
        y += dy
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
}