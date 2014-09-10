package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class NearbyTestWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> locationIds;
    private final AtomicInteger counter;

    private final Random random;
    public boolean running;

    public NearbyTestWorker(ProxStorConnector conn, List<String> userIds, AtomicInteger counter) {
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
        String locId2;
        do {
            locId = locationIds.get(random.nextInt(locationIds.size()));
            locId2 = locationIds.get(random.nextInt(locationIds.size()));
            distance = random.nextInt(1000 + 1);
            boolean status;
            status = conn.isNearby(Integer.parseInt(locId), Integer.parseInt(locId2), distance);
            counter.getAndIncrement();
        } while (running);
    }

}
