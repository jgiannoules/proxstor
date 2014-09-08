package com.giannoules.proxstor.testing.loader;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SensorAddWorker implements Runnable {

    private final Location l;
    private final Sensor s;
    private final ProxStorConnector conn;
    private final AtomicInteger operations;

    public SensorAddWorker(Location l, Sensor s, ProxStorConnector conn, AtomicInteger operations) {
        this.l = l;
        this.s = s;
        this.conn = conn;
        this.operations = operations;
    }

    @Override
    public void run() {
        try {
            s.setSensorId(conn.putSensor(Integer.parseInt(l.getLocId()), s).getSensorId());
            operations.getAndIncrement();
        } catch (Exception ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
