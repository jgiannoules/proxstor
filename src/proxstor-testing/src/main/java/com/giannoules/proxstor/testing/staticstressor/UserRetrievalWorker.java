package com.giannoules.proxstor.testing.staticstressor;

import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class UserRetrievalWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> userIds;
    private final AtomicInteger counter;

    private final Random random;
    public boolean running;

    public UserRetrievalWorker(ProxStorConnector conn, List<String> userIds, AtomicInteger counter) {
        this.conn = conn;
        this.userIds = userIds;
        this.counter = counter;
        random = new Random();
        running = true;
    }

    @Override
    public void run() {
        String userId;
        User u;
        do {
            userId = userIds.get(random.nextInt(userIds.size()));
            u = conn.getUser(Integer.parseInt(userId));
            counter.getAndIncrement();
        } while (running);
    }

}
