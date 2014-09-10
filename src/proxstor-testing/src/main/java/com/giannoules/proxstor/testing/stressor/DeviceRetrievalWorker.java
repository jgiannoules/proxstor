package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceRetrievalWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> userIds;
    private final AtomicInteger counter;

    private final Random random;
    public boolean running;

    public DeviceRetrievalWorker(ProxStorConnector conn, List<String> userIds, AtomicInteger counter) {
        this.conn = conn;
        this.userIds = userIds;
        this.counter = counter;
        random = new Random();
        running = true;
    }

    @Override
    public void run() {
        String userId;
        Collection<Device> devices;
        do {
            userId = userIds.get(random.nextInt(userIds.size()));
            devices = conn.getDevices(Integer.parseInt(userId));
            counter.getAndIncrement();
        } while (running);
    }

}
