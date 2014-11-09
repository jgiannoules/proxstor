package com.giannoules.proxstor.testing.loader;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import com.giannoules.proxstor.testing.ReaderWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Loader {

    static int THREAD_COUNT = 128;

    static List<User> users;
    static List<Device> devices;
    static List<Location> locations;
    static List<Environmental> environmentals;

    static Map<String, User> userMap = new HashMap<>();
    static Map<String, Device> deviceMap = new HashMap<>();
    static Map<String, Location> locationMap = new HashMap<>();
    static Map<String, Environmental> environmentalMap = new HashMap<>();

    static ProxStorConnector conn;

    static MonitorWorker mw;
    static Thread monitorThread;

    static AtomicInteger operations = new AtomicInteger();

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("must provide directory");
            return;
        }

        String dir = args[0];

        System.out.println("ProxStor Static Content Loader");
        System.out.println("==============================");
        System.out.println("THREAD_COUNT = " + THREAD_COUNT);
        System.out.println();

        users = readFromFile(dir, "users.json", User.class);
        devices = readFromFile(dir, "devices.json", Device.class);
        locations = readFromFile(dir, "locations.json", Location.class);
        environmentals = readFromFile(dir, "environmentals.json", Environmental.class);
        System.out.println();

        conn = new ProxStorConnector("http://localhost:8080/api");

        buildMaps();    // build Maps used by Loader to correlate objects
        addUsers();     // insert all users into graph
        addDevices();   // insert users' devices
        addKnows();     // associate each user with knows relationship

        addLocations(); // insert all locations into graph        
        addEnvironmentals();   // insert locations' environmentals
        addWithin();    // associate locations within locations
        addNearby();    // set distance from location to location for those nearby
        
        writeIds(dir);
        
    }

    private static AtomicInteger startMonitoring() {
        mw = new MonitorWorker();
        monitorThread = new Thread(mw);
        monitorThread.start();
        return mw.getCounter();
    }

    private static void stopMontoring() {
        mw.done();
        try {
            monitorThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void addUsers() {
        System.out.println("Loading users into graph...");
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger counter = startMonitoring();
        for (User u : users) {
            Runnable worker = new UserAddWorker(u, conn, counter);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        stopMontoring();
        System.out.println("User load complete.");
        System.out.println();
    }

    private static void addDevices() {
        System.out.println("Loading devices into graph...");
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger counter = startMonitoring();
        for (User u : users) {
            for (String devId : u.devices) {
                Device d = deviceMap.get(devId);
                Runnable worker = new DeviceAddWorker(u, d, conn, counter);
                executor.execute(worker);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        stopMontoring();
        System.out.println("Device load complete.");
        System.out.println();
    }

    private static void addKnows() {
        System.out.println("Setting users knows relationships...  [THREAD POOL SIZE 1]");
        ExecutorService executor = Executors.newFixedThreadPool(1); // @TODO fix this back to THREAD_COUNT
        AtomicInteger counter = startMonitoring();
        for (User u : users) {
            if (u.knows != null) {
                for (int i = 0; i < u.knows.size(); i++) {
                    User v = userMap.get(u.knows.get(i));
                    int strength = u.strength.get(i);
                    Runnable worker = new UserKnowsWorker(u, v, strength, conn, counter);
                    executor.execute(worker);
                }
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        stopMontoring();
        System.out.println("Knows load complete.");
        System.out.println();
    }

    private static void addLocations() {
        System.out.println("Loading locations into graph...");
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger counter = startMonitoring();
        for (Location l : locations) {
            Runnable worker = new LocationAddWorker(l, conn, counter);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        stopMontoring();
        System.out.println("Location load complete.");
        System.out.println();
    }

    private static void addEnvironmentals() {
        System.out.println("Loading environmental into graph...");
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger counter = startMonitoring();
        for (Location l : locations) {
            for (String environmentalId : l.environmentals) {
                Environmental s = environmentalMap.get(environmentalId);
                Runnable worker = new EnvironmentalAddWorker(l, s, conn, counter);
                executor.execute(worker);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        stopMontoring();
        System.out.println("Environmental load complete.");
        System.out.println();
    }

    private static void addWithin() {
        System.out.println("Setting location within relationships... [THREAD POOL SIZE 1]");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        AtomicInteger counter = startMonitoring();
        for (Location l : locations) {
            if (l.within != null) {
                Iterator i = l.within.iterator();
                while (i.hasNext()) {                
                    Location v = locationMap.get(i.next().toString());                    
                    Runnable worker = new LocationWithinWorker(l, v, conn, counter);
                    executor.execute(worker);
                }
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        stopMontoring();
        System.out.println("Within load complete.");
        System.out.println();
    }
    
    private static void addNearby() {
        System.out.println("Setting location nearby relationships... [THREAD POOL SIZE 1]");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        AtomicInteger counter = startMonitoring();
        for (Location l : locations) {
            if (l.nearbyLocId != null) {
                for (int i = 0; i < l.nearbyLocId.size(); i++) {
                    Location v = locationMap.get(l.nearbyLocId.get(i));
                    Integer d = l.nearbyDistance.get(i);
                    Runnable worker = new LocationNearbyWorker(l, v, d, conn, counter);
                    executor.execute(worker);
                }
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        stopMontoring();
        System.out.println("Nearby load complete.");
        System.out.println();
    }
        
    /*
     * using ReaderWriter class read in List<T>
     */
    private static <T> List<T> readFromFile(String dir, String file, Class c) {
        List<T> l;
        System.out.print("Reading " + file + "...");
        long startTime = System.currentTimeMillis();
        l = ReaderWriter.read(dir + "/" + file, c);
        long endTime = System.currentTimeMillis();
        if (l != null) {
            System.out.print("done.");
        } else {
            System.out.print("failure.");
        }
        System.out.println(" (" + (endTime - startTime) + " ms, " + l.size() + " entries)");
        return l;
    }
    
    private static void writeIds(String dir) {
        List<String> ids = new ArrayList<>();
        for (User u : users) {
            ids.add(u.getUserId());
        }
        writeToFile(dir, "userIds.json", ids);
        ids.clear();
        for (Device d : devices) {
            ids.add(d.getDevId());
        }
        writeToFile(dir, "devIds.json", ids);
        ids.clear();
        for (Location l : locations) {
            ids.add(l.getLocId());
        }
        writeToFile(dir, "locationIds.json", ids);
        ids.clear();
        for (Environmental s : environmentals) {
            ids.add(s.environmentalId);
        }
        writeToFile(dir, "environmentalIds.json", ids);
    }
    
    /*
     * using ReaderWriter class output List<T> to a file
     */
    private static <T> void writeToFile(String dir, String file, List<T> l) {
        System.out.print("Creating " + file + "...");
        long startTime = System.currentTimeMillis();
        boolean status = ReaderWriter.write(dir + "/" + file, l);
        long endTime = System.currentTimeMillis();
        if (status) {
            System.out.print("done.");
        } else {
            System.out.print("failure.");
        }
        System.out.println(" (" + (endTime - startTime) + " ms)");
    }

    /*
     * map random generated UUIDs back to users/devices since the *Id values
     * will be turned into *real* IDs once the additions to the graph happen
     */
    public static void buildMaps() {
        for (User u : users) {
            userMap.put(u.getUserId(), u);
        }
        System.out.println("Created user map");
        for (Device d : devices) {
            deviceMap.put(d.getDevId(), d);
        }
        System.out.println("Created device map");
        for (Location l : locations) {
            locationMap.put(l.getLocId(), l);
        }
        System.out.println("Created location map");
        for (Environmental s : environmentals) {
            environmentalMap.put(s.getEnvironmentalId(), s);
        }
        System.out.println("Created environmentals map");
        System.out.println();
    }
     
}
