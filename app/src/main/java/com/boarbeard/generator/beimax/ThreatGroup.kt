package com.boarbeard.generator.beimax

import com.boarbeard.generator.beimax.event.Threat

class ThreatGroup {
    var internal: Threat? = null
    var external: Threat? = null

    constructor()
    constructor(e: Threat) {
        set(e)
    }
    constructor(internal: Threat?, external: Threat?) {
        this.internal = internal
        this.external = external
    }

    fun hasInternal(): Boolean {
        return internal != null
    }

    fun hasExternal(): Boolean {
        return external != null
    }

    fun addInternal(i: Threat?): Boolean {
        if (internal == null) {
            internal = i
            return true
        }
        return false
    }

    fun addExternal(e: Threat?): Boolean {
        if (external == null) {
            external = e
            return true
        }
        return false
    }

    fun set(e: Threat) {
        if (e.threatPosition == Threat.THREAT_POSITION_INTERNAL) {
            internal = e
        } else {
            external = e
        }
    }

    fun removeInternal(): Threat? {
        val ret = internal
        internal = null
        return ret
    }

    fun removeExternal(): Threat? {
        val ret = external
        external = null
        return ret
    }

    override fun toString(): String {
        return "internal: $internal + external: $external"
    }
}