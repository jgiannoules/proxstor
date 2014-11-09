package com.giannoules.proxstor.testing.staticstressor;

import com.giannoules.proxstor.connection.ProxStorConnector;
import com.giannoules.proxstor.testing.ReaderWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class StaticStressor {

    private static final int USER_RETRIEVAL_WORKERS = 32;
    private static final int DEVICE_RETRIEVAL_WORKERS = 32;
    private static final int KNOWS_RETRIEVAL_WORKERS = 128;
    private static final int LOCATION_RETRIEVAL_WORKERS = 128;
    private static final int ENVIRONMENTAL_RETRIEVAL_WORKERS = 128;
    private static final int NEARBY_RETRIEVAL_WORKERS = 128;
    private static final int WITHIN_RETRIEVAL_WORKERS = 128;
    private static final int WITHINREV_RETRIEVAL_WORKERS = 64;    
    private static final int WITHINTEST_RETRIEVAL_WORKERS = 16;
    private static final int NEARBYTEST_RETRIEVAL_WORKERS = 16;

    static List<String> userIds;
    static List<String> deviceIds;
    static List<String> locationIds;
    static List<String> environmentalIds;

    static ProxStorConnector conn;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("must provide directory");
            return;
        }

        String dir = args[0];

        System.out.println("ProxStor Static Content Stressor");
        System.out.println("================================");
        System.out.println();

        userIds = ReaderWriter.read(dir + "/userIds.json", String.class);
        deviceIds = ReaderWriter.read(dir + "/devIds.json", String.class);
        locationIds = ReaderWriter.read(dir + "/locationIds.json", String.class);
        environmentalIds = ReaderWriter.read(dir + "/environmentalIds.json", String.class);
        System.out.println();

        conn = new ProxStorConnector("http://localhost:8080/api");

        StressorMonitorWorker smw = new StressorMonitorWorker();

        System.out.println("Creating User Retrieval Workers @ " + USER_RETRIEVAL_WORKERS + " threads");
        ExecutorService userRetrievalExecutor = Executors.newFixedThreadPool(USER_RETRIEVAL_WORKERS);
        AtomicInteger counter = smw.register("User Retrieval:\t\t");
        for (int i = 0; i < USER_RETRIEVAL_WORKERS; i++) {
            UserRetrievalWorker worker = new UserRetrievalWorker(conn, userIds, counter);
            userRetrievalExecutor.execute(worker);
        }
        
        System.out.println("Creating Device Retrieval Workers @ " + DEVICE_RETRIEVAL_WORKERS + " threads");
        ExecutorService deviceRetrievalExecutor = Executors.newFixedThreadPool(DEVICE_RETRIEVAL_WORKERS);
        counter = smw.register("Device Retrieval:\t");
        for (int i = 0; i < DEVICE_RETRIEVAL_WORKERS; i++) {
            DeviceRetrievalWorker worker = new DeviceRetrievalWorker(conn, userIds, counter);
            deviceRetrievalExecutor.execute(worker);
        }
        
        System.out.println("Creating Knows Retrieval Workers @ " + KNOWS_RETRIEVAL_WORKERS + " threads");
        ExecutorService knowsRetrievalExecutor = Executors.newFixedThreadPool(KNOWS_RETRIEVAL_WORKERS);
        counter = smw.register("Knows Retrieval:\t");
        for (int i = 0; i < KNOWS_RETRIEVAL_WORKERS; i++) {
            KnowsRetrievalWorker worker = new KnowsRetrievalWorker(conn, userIds, counter);
            knowsRetrievalExecutor.execute(worker);
        }

        System.out.println("Creating Location Retrieval Workers @ " + LOCATION_RETRIEVAL_WORKERS + " threads");
        ExecutorService locationRetrievalExecutor = Executors.newFixedThreadPool(LOCATION_RETRIEVAL_WORKERS);
        counter = smw.register("Location Retrieval:\t");
        for (int i = 0; i < LOCATION_RETRIEVAL_WORKERS; i++) {
            LocationRetrievalWorker worker = new LocationRetrievalWorker(conn, locationIds, counter);
            locationRetrievalExecutor.execute(worker);
        }
        
        System.out.println("Creating Environmental Retrieval Workers @ " + ENVIRONMENTAL_RETRIEVAL_WORKERS + " threads");
        ExecutorService environmentalRetrievalExecutor = Executors.newFixedThreadPool(ENVIRONMENTAL_RETRIEVAL_WORKERS);
        counter = smw.register("Environmental Retrieval:\t");
        for (int i = 0; i < ENVIRONMENTAL_RETRIEVAL_WORKERS; i++) {
            EnvironmentalRetrievalWorker worker = new EnvironmentalRetrievalWorker(conn, locationIds, counter);
            environmentalRetrievalExecutor.execute(worker);
        }
        
        System.out.println("Creating Nearby Retrieval Workers @ " + NEARBY_RETRIEVAL_WORKERS + " threads");
        ExecutorService nearbyRetrievalExecutor = Executors.newFixedThreadPool(NEARBY_RETRIEVAL_WORKERS);
        counter = smw.register("Nearby Retrieval:\t");
        for (int i = 0; i < NEARBY_RETRIEVAL_WORKERS; i++) {
            NearbyRetrievalWorker worker = new NearbyRetrievalWorker(conn, locationIds, counter);
            nearbyRetrievalExecutor.execute(worker);
        }
        
        System.out.println("Creating Within Retrieval Workers @ " + WITHIN_RETRIEVAL_WORKERS + " threads");
        ExecutorService withinRetrievalExecutor = Executors.newFixedThreadPool(WITHIN_RETRIEVAL_WORKERS);
        counter = smw.register("Within Retrieval:\t");
        for (int i = 0; i < WITHIN_RETRIEVAL_WORKERS; i++) {
            WithinRetrievalWorker worker = new WithinRetrievalWorker(conn, locationIds, counter);
            withinRetrievalExecutor.execute(worker);
        }

        System.out.println("Creating Reverse Within Retrieval Workers @ " + WITHINREV_RETRIEVAL_WORKERS + " threads");
        ExecutorService withinReverseRetrievalExecutor = Executors.newFixedThreadPool(WITHINREV_RETRIEVAL_WORKERS);
        counter = smw.register("Within Reverse Retrieval:");
        for (int i = 0; i < WITHINREV_RETRIEVAL_WORKERS; i++) {
            WithinReverseRetrievalWorker worker = new WithinReverseRetrievalWorker(conn, locationIds, counter);
            withinReverseRetrievalExecutor.execute(worker);
        }
        
        System.out.println("Creating Is Within Test Workers @ " + WITHINTEST_RETRIEVAL_WORKERS + " threads");
        ExecutorService withinTestExecutor = Executors.newFixedThreadPool(WITHINTEST_RETRIEVAL_WORKERS);
        counter = smw.register("Within Test:\t\t");
        for (int i = 0; i < WITHINTEST_RETRIEVAL_WORKERS; i++) {
            WithinTestWorker worker = new WithinTestWorker(conn, locationIds, counter);
            withinTestExecutor.execute(worker);
        }
        
        System.out.println("Creating Is Nearby Test Workers @ " + NEARBYTEST_RETRIEVAL_WORKERS + " threads");
        ExecutorService nearbyTestExecutor = Executors.newFixedThreadPool(NEARBYTEST_RETRIEVAL_WORKERS);
        counter = smw.register("Nearby Test:\t\t");
        for (int i = 0; i < NEARBYTEST_RETRIEVAL_WORKERS; i++) {
            NearbyTestWorker worker = new NearbyTestWorker(conn, locationIds, counter);
            nearbyTestExecutor.execute(worker);
        }

        System.out.println("Starting Monitor Worker....\n");
        smw.run();
    }   
}
