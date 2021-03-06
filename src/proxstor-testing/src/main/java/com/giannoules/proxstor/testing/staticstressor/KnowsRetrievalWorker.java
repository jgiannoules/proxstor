package com.giannoules.proxstor.testing.staticstressor;

import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class KnowsRetrievalWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> userIds;
    private final AtomicInteger counter;

    private final Random random;
    public boolean running;

    public KnowsRetrievalWorker(ProxStorConnector conn, List<String> userIds, AtomicInteger counter) {
        this.conn = conn;
        this.userIds = userIds;
        this.counter = counter;
        random = new Random();
        running = true;
    }

    @Override
    public void run() {
        int strength;
        String userId;
        Collection<User> users;
        do {
            userId = userIds.get(random.nextInt(userIds.size()));
            strength = random.nextInt(101);
            users = conn.getKnows(userId, strength);
            counter.getAndIncrement();
        } while (running);
    }

}
