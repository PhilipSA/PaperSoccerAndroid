package com.ps.simplepapersoccer.data.enums

//Don't steal from ConnectionTypeEnum values
enum class NodeTypeEnum(val normalizedIdentiferValue: Float) {
    Empty(0.11f),
    Wall(0.21f),
    ContainsBall(0.31f),
    BounceAble(0.41f),
    Goal(0.51f),
    Post(0.61f)
}
