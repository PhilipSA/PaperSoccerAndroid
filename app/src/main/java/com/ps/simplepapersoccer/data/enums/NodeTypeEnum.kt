package com.ps.simplepapersoccer.data.enums

enum class NodeTypeEnum(val normalizedIdentiferValue: Int) {
    Empty(-1),
    Wall(-2),
    ContainsBall(-3),
    BounceAble(-4),
    Goal(-5),
    Post(-6)
}
