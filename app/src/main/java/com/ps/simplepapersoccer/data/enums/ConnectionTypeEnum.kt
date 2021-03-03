package com.ps.simplepapersoccer.data.enums

enum class ConnectionTypeEnum(val normalizedIdentiferValue: Float) {
    Open(0.1f),
    Blocked(0.2f),
    LineTiltedLeft(0.3f),
    LineTiltedRight(0.4f)
}