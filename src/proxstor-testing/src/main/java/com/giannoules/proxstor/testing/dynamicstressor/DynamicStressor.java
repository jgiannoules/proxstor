package com.giannoules.proxstor.testing.dynamicstressor;

import com.giannoules.proxstor.connection.ProxStorConnector;
import com.giannoules.proxstor.testing.ReaderWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicStressor {

    private static final int USER_CHECKIN_LOCATION_WORKERS = 128; 
    private static final int DEVICE_DISCOVERS_SENSORID_WORKERS = 32;

    static List<String> userIds;
    static List<String> deviceIds;
    static List<String> locationIds;
    static List<String> sensorIds;

    static ProxStorConnector conn;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("must provide directory");
            return;
        }

        String dir = args[0];

        System.out.println("ProxStor Dynamic Content Stressor");
        System.out.println("=================================");
        System.out.println();

        userIds = ReaderWriter.read(dir + "/userIds.json", String.class);
        deviceIds = ReaderWriter.read(dir + "/devIds.json", String.class);
        locationIds = ReaderWriter.read(dir + "/locationIds.json", String.class);
        sensorIds = ReaderWriter.read(dir + "/sensorIds.json", String.class);
        System.out.println();

        conn = new ProxStorConnector("http://localhost:8080/api");

        StressorMonitorWorker smw = new StressorMonitorWorker();

        System.out.println("Creating User Checkin Location Workers @ " + USER_CHECKIN_LOCATION_WORKERS + " threads");
        ExecutorService userCheckinLocationExecutor = Executors.newFixedThreadPool(USER_CHECKIN_LOCATION_WORKERS);
        AtomicInteger counter = smw.register("User Checkin Location:\t\t");        
        for (int i = 0; i < USER_CHECKIN_LOCATION_WORKERS; i++) {
            UserCheckinLocationWorker worker = new UserCheckinLocationWorker(conn, userIds.get(i), locationIds, counter);
            userCheckinLocationExecutor.execute(worker);
        }      

        System.out.println("Creating Device Discovers Sensor ID Workers @ " + DEVICE_DISCOVERS_SENSORID_WORKERS + " threads");
        ExecutorService deviceDiscoverSensorIdExecutor = Executors.newFixedThreadPool(DEVICE_DISCOVERS_SENSORID_WORKERS);
        counter = smw.register("Device Discover Sensor ID:\t");        
        for (int i = 0; i < DEVICE_DISCOVERS_SENSORID_WORKERS; i++) {
            DeviceDetectsSensorIdWorker worker = new DeviceDetectsSensorIdWorker(conn, deviceIds.get(i), sensorIds, counter);
            deviceDiscoverSensorIdExecutor.execute(worker);
        }      
        
        System.out.println("Starting Monitor Worker....\n");
        smw.run();
    }   
}
