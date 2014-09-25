package com.giannoules.proxstor.testing.staticstressor;

import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class SensorRetrievalWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> locationIds;
    private final AtomicInteger counter;

    private final Random random;
    public boolean running;

    public SensorRetrievalWorker(ProxStorConnector conn, List<String> userIds, AtomicInteger counter) {
        this.conn = conn;
        this.locationIds = userIds;
        this.counter = counter;
        random = new Random();
        running = true;
    }

    @Override
    public void run() {
        String locId;
        Collection<Sensor> sensors;
        do {
            locId = locationIds.get(random.nextInt(locationIds.size()));
            sensors = conn.getSensors(Integer.parseInt(locId));
            counter.addAndGet(sensors.size());                      
        } while (running);
    }

}
