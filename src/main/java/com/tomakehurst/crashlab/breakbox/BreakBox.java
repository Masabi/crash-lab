package com.tomakehurst.crashlab.breakbox;

import com.ning.http.client.AsyncHttpClient;

import java.util.List;

import static java.util.Arrays.asList;

public class BreakBox {

    private final String name;

    private final Fault.Direction direction;
    private final Fault.Protocol protocol;
    private final int destPort;
    private final BreakBoxAdminClient adminClient;

    public BreakBox(String name, Fault.Direction direction, Fault.Protocol protocol, int destPort, List<String> hosts) {
        this.name = name;
        this.direction = direction;
        this.protocol = protocol;
        this.destPort = destPort;
        adminClient = new BreakBoxAdminClient(new AsyncHttpClient(), hosts);
    }

    public static BreakBox defineClient(String name, int destPort, String... hosts) {
        return new BreakBox(name, Fault.Direction.OUT, Fault.Protocol.TCP, destPort, asList(hosts));
    }

    public static BreakBox defineClient(String name, Fault.Protocol protocol, int destPort, String... hosts) {
        return new BreakBox(name, Fault.Direction.OUT, protocol, destPort, asList(hosts));
    }

    public static BreakBox defineService(String name, int destPort, String... hosts) {
        return new BreakBox(name, Fault.Direction.IN, Fault.Protocol.TCP, destPort, asList(hosts));
    }

    public static BreakBox defineService(String name, Fault.Protocol protocol, int destPort, String... hosts) {
        return new BreakBox(name, Fault.Direction.IN, protocol, destPort, asList(hosts));
    }

    public void reset() {
        adminClient.reset();
    }

    public <T extends Fault> void addFault(T fault) {
        fault.setDirection(direction);
        fault.setProtocol(protocol);
        fault.setToPort(destPort);
        adminClient.addFault(fault);
    }


}
