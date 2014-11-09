package com.giannoules.proxstor.testing.loader;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnvironmentalAddWorker implements Runnable {

    private final Location l;
    private final Environmental s;
    private final ProxStorConnector conn;
    private final AtomicInteger operations;

    public EnvironmentalAddWorker(Location l, Environmental s, ProxStorConnector conn, AtomicInteger operations) {
        this.l = l;
        this.s = s;
        this.conn = conn;
        this.operations = operations;
    }

    @Override
    public void run() {
        try {
            s.setEnvironmentalId(conn.addEnvironmental(Integer.parseInt(l.getLocId()), s).getEnvironmentalId());
            operations.getAndIncrement();
        } catch (NumberFormatException ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
