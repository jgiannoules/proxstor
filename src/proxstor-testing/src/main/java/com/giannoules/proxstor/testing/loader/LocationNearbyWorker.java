package com.giannoules.proxstor.testing.loader;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationNearbyWorker implements Runnable {

    private final Location l;
    private final Location v;
    private final int d;
    private final ProxStorConnector conn;
    private final AtomicInteger operations;

    public LocationNearbyWorker(Location l, Location v, int d, ProxStorConnector conn, AtomicInteger operations) {
        this.l = l;
        this.v = v;
        this.d = d;
        this.operations = operations;
        this.conn = conn;
    }

    @Override
    public void run() {
        conn.locationNearby(l, v, d);
        operations.getAndIncrement();
    }
}
