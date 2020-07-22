package com.ps.simplepapersoccer.data.enums

enum class ConnectionTypeEnum(val normalizedIdentiferValue: Double) {
    Open(0.1),
    Blocked(0.2),
    LineTiltedLeft(0.3),
    LineTiltedRight(0.4)
}