package com.giannoules.proxstor.testing.staticstressor;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class WithinRetrievalWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> locationIds;
    private final AtomicInteger counter;

    private final Random random;
    public boolean running;

    public WithinRetrievalWorker(ProxStorConnector conn, List<String> userIds, AtomicInteger counter) {
        this.conn = conn;
        this.locationIds = userIds;
        this.counter = counter;
        random = new Random();
        running = true;
    }

    @Override
    public void run() {
        String locId;
        Collection<Location> locations;
        do {
            locId = locationIds.get(random.nextInt(locationIds.size()));
            locations = conn.getLocationsWithin(Integer.parseInt(locId));
            counter.addAndGet(locations.size());
        } while (running);
    }

}
