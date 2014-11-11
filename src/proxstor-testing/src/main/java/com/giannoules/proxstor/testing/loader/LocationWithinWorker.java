package com.giannoules.proxstor.testing.loader;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationWithinWorker implements Runnable {

    private final ProxStorConnector conn;
    private final AtomicInteger operations;

    private final Location l;
    private final Location v;

    public LocationWithinWorker(Location l, Location v, ProxStorConnector conn, AtomicInteger operations) {
        this.l = l;
        this.v = v;
        this.conn = conn;
        this.operations = operations;
    }

    @Override
    public void run() {
        do {
        } while (!conn.addLocationWithin(l.getLocId(), v.getLocId()));
        operations.getAndIncrement();
    }
}
