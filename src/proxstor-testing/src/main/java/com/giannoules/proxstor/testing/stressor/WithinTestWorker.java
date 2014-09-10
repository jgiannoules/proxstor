package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class WithinTestWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> locationIds;
    private final AtomicInteger counter;

    private final Random random;
    public boolean running;

    public WithinTestWorker(ProxStorConnector conn, List<String> userIds, AtomicInteger counter) {
        this.conn = conn;
        this.locationIds = userIds;
        this.counter = counter;
        random = new Random();
        running = true;
    }

    @Override
    public void run() {
        String locId;
        String locId2;
        boolean result;
        do {
            locId = locationIds.get(random.nextInt(locationIds.size()));
            locId2 = locationIds.get(random.nextInt(locationIds.size()));
            result = conn.isWithin(Integer.parseInt(locId), Integer.parseInt(locId2));
            counter.incrementAndGet();
        } while (running);
    }

}
