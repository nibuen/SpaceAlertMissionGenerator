package com.boarbeard.generator.beimax

class MissionPreferences {
    /** debug flag to condense output for easy readability */
    var compressTime = false

    var seed: Long = 0

    var players = 5
        set(value) {
            require(players in 1..5)
            field = value
        }

    /**
     * If true, then unconfirmed reports will show up as unconfirmed
     * reports; if false, then they'll either show up as normal
     * ("confirmed") threats, or not at all, depending on the player count.
     */
    var showUnconfirmed = false
    var enableDoubleThreats = false

    /**
     * configuration: threat level (8 for std game)
     */
    var threatLevel = 8

    /**
     * ...of which 1 level is unconfirmed (for 5 players)
     */
    var threatUnconfirmed = 1

    var incomingDataRange = IntRange(1, 6)

    fun getMinIncomingData(): Int {
        return incomingDataRange.first
    }

    fun getMaxIncomingData(): Int {
        return incomingDataRange.last
    }

    /**
     * minimum and maximum time for phases
     */
    var minPhaseTime = intArrayOf(205, 180, 140)
    var maxPhaseTime = intArrayOf(240, 225, 155)
}