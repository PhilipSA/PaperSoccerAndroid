package com.ps.simplepapersoccer.data.enums

enum class NodeTypeEnum(val normalizedIdentiferValue: Double) {
    Empty(-0.1),
    Wall(-0.2),
    ContainsBall(-0.3),
    BounceAble(-0.4),
    Goal(-0.5),
    Post(-0.6)
}
