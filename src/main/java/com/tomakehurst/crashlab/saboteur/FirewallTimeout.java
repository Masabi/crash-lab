package com.tomakehurst.crashlab.saboteur;

import com.tomakehurst.crashlab.TimeInterval;

import java.util.concurrent.TimeUnit;

import static com.tomakehurst.crashlab.TimeInterval.interval;
import static java.util.concurrent.TimeUnit.SECONDS;

public class FirewallTimeout extends Fault {

    private TimeInterval tcpTimeout;

    public static FirewallTimeout firewallTimeout(String name) {
        FirewallTimeout firewallTimeout = new FirewallTimeout();
        firewallTimeout.setName(name);
        return firewallTimeout;
    }

    @Override
    public Type getType() {
        return Type.FIREWALL_TIMEOUT;
    }

    public FirewallTimeout timeout(long time, TimeUnit timeUnit) {
        this.tcpTimeout = interval(time, timeUnit);
        return this;
    }

    public long getTimeout() {
        return tcpTimeout.timeIn(SECONDS);
    }
}
