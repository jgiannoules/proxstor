package com.giannoules.proxstor.testing.dynamicstressor;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceDetectsEnvironmentalIdWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> environmentalIds;
    private final AtomicInteger counter;

    private final Random random;
    private final String devId;
    
    public boolean running;

    public DeviceDetectsEnvironmentalIdWorker(ProxStorConnector conn, String devId, List<String> environmentalIds, AtomicInteger counter) {
        this.conn = conn;
        this.devId = devId;
        this.environmentalIds = environmentalIds;
        this.counter = counter;
        random = new Random();
        running = true;        
    }

    @Override
    public void run() {
        String environmentalId;
        Locality l;
        do {
            environmentalId = environmentalIds.get(random.nextInt(environmentalIds.size()));
            l = conn.deviceDetectsEnvironmentalId(devId, environmentalId);
            conn.deviceUndetectsEnvironmentalId(devId, environmentalId);
            counter.getAndIncrement();
        } while (running);
    }

}
