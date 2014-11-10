package com.giannoules.proxstor.testing.dynamicstressor;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class UserCheckinLocationWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> locIds;
    private final AtomicInteger counter;

    private final Random random;
    private final String userId;
    
    public boolean running;

    public UserCheckinLocationWorker(ProxStorConnector conn, String userId, List<String> locIds, AtomicInteger counter) {
        this.conn = conn;
        this.userId = userId;
        this.locIds = locIds;
        this.counter = counter;
        random = new Random();
        running = true;        
    }

    @Override
    public void run() {
        String locId;
        Locality l;
        do {
            locId = locIds.get(random.nextInt(locIds.size()));
            l = conn.userCheckinLocation(userId, locId);
            conn.userCheckoutLocation(userId, locId);
            counter.getAndIncrement();
        } while (running);
    }

}
