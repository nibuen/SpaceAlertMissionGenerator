package com.boarbeard.generator.beimax;

import com.boarbeard.generator.beimax.event.Threat;

public class ThreatGroup {
    private Threat internal;
    private Threat external;

    public ThreatGroup() {
    }

    public ThreatGroup(Threat e) {
        set(e);
    }

    public Threat getInternal() {
        return internal;
    }

    public Threat getExternal() {
        return external;
    }

    public boolean hasInternal() {
        return internal != null;
    }

    public boolean hasExternal() {
        return external != null;
    }

    public boolean addInternal(Threat i) {
        if (internal == null) {
            internal = i;
            return true;
        }
        return false;
    }

    public boolean addExternal(Threat e) {
        if (external == null) {
            external = e;
            return true;
        }
        return false;
    }

    public void setExternal(Threat e) {
        external = e;
    }

    public void setInternal(Threat e) {
        internal = e;
    }

    public void set(Threat e) {
        if (e.getThreatPosition() == Threat.THREAT_POSITION_INTERNAL) {
            setInternal(e);
        } else {
            setExternal(e);
        }
    }

    public Threat removeInternal() {
        Threat ret = internal;
        internal = null;
        return ret;
    }

    public Threat removeExternal() {
        Threat ret = external;
        external = null;
        return ret;
    }
}