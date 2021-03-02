package com.ps.simplepapersoccer.gameObjects.game.geometry

data class TwoDimensionalPointF(var x: Float, var y: Float) {

    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun equals(x: Float, y: Float): Boolean {
        return this.x == x && this.y == y
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val point = other as TwoDimensionalPointF

        if (x != point.x) return false
        return y == point.y
    }

    override fun hashCode(): Int {
        var result = if (x != +0.0f) java.lang.Float.floatToIntBits(x) else 0
        result = 31 * result + if (y != +0.0f) java.lang.Float.floatToIntBits(y) else 0
        return result
    }

    override fun toString(): String {
        return "`TwoDimensionalPoint`($x, $y)"
    }
}