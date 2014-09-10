package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationRetrievalWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> locationIds;
    private final AtomicInteger counter;

    private final Random random;
    public boolean running;

    public LocationRetrievalWorker(ProxStorConnector conn, List<String> userIds, AtomicInteger counter) {
        this.conn = conn;
        this.locationIds = userIds;
        this.counter = counter;
        random = new Random();
        running = true;
    }

    @Override
    public void run() {
        int distance;
        String locId;
        Location l;
        do {
            locId = locationIds.get(random.nextInt(locationIds.size()));
            distance = random.nextInt(1000 + 1);
            l = conn.getLocation(Integer.parseInt(locId));
            counter.getAndIncrement();         
        } while (running);
    }

}
