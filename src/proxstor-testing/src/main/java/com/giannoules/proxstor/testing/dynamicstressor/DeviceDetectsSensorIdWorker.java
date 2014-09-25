package com.giannoules.proxstor.testing.dynamicstressor;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceDetectsSensorIdWorker implements Runnable {

    private final ProxStorConnector conn;
    private final List<String> sensorIds;
    private final AtomicInteger counter;

    private final Random random;
    private final String devId;
    
    public boolean running;

    public DeviceDetectsSensorIdWorker(ProxStorConnector conn, String devId, List<String> sensorIds, AtomicInteger counter) {
        this.conn = conn;
        this.devId = devId;
        this.sensorIds = sensorIds;
        this.counter = counter;
        random = new Random();
        running = true;        
    }

    @Override
    public void run() {
        String sensorId;
        Locality l;
        do {
            sensorId = sensorIds.get(random.nextInt(sensorIds.size()));
            l = conn.deviceDetectsSensorId(Integer.parseInt(devId), Integer.parseInt(sensorId));
            conn.deviceUndetectsSensorId(Integer.parseInt(devId), Integer.parseInt(sensorId));
            counter.getAndIncrement();
        } while (running);
    }

}
