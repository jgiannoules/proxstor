package com.giannoules.proxstor.testing.loader;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationAddWorker implements Runnable {

    public Location l;
    private final ProxStorConnector conn;
    private final AtomicInteger operations;

    public LocationAddWorker(Location l, ProxStorConnector conn, AtomicInteger operations) {
        this.l = l;
        this.operations = operations;
        this.conn = conn;
    }

    @Override
    public void run() {
        l.setLocId(conn.addLocation(l).getLocId());
        operations.getAndIncrement();
    }
}
