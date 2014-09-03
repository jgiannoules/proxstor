package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.text.DecimalFormat;
import java.util.HashMap;
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
    static List<Sensor> sensors;

    static Map<String, User> userMap = new HashMap<>();
    static Map<String, Device> deviceMap = new HashMap<>();
    
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
        sensors = readFromFile(dir, "sensors.json", Sensor.class);
        System.out.println();
        
        conn = new ProxStorConnector("http://localhost:8080/api");

        buildMaps();    // build Maps used by Loader to correlate objects
        addUsers();     // insert all users into graph
        addDevices();   // insert users' devices
        addKnows();     // associate each user with knows relationship
        /*
        addLocations(); // insert all locations into graph
        addSensors();   // insert locations' sensors
        add
        */
    }

    private static void startMonitoring() {
        operations.set(0);
        mw = new MonitorWorker();
        monitorThread = new Thread(mw);
        monitorThread.start();
    }

    private static void stopMontoring() {
        mw.go = false;
        try {
            monitorThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void addUsers() {
        System.out.println("Loading users into graph...");
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        startMonitoring();
        for (User u : users) {
            Runnable worker = new UserAddWorker(u);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) { }       
        stopMontoring();
        System.out.println("User load complete.");
        System.out.println();
    }
    
    private static void addDevices() {
        System.out.println("Loading devices into graph...");
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        startMonitoring();
        for (User u : users) {
            for (String devId : u.devices) {
                Device d = deviceMap.get(devId);
                Runnable worker = new DeviceAddWorker(u, d);
                executor.execute(worker);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {  }
        stopMontoring();
        System.out.println("Device load complete.");
        System.out.println();
    }

    private static void addKnows() {
        System.out.println("Setting users knows relationships...");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        startMonitoring();
        for (User u : users) {
            if (u.knows != null) {
                for (int i = 0; i < u.knows.size(); i++) {
                    User v = userMap.get(u.knows.get(i));
                    int strength = u.strength.get(i);                    
                    Runnable worker = new UserKnowsWorker(u, v, strength);
                    executor.execute(worker);
                }
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {  }
        stopMontoring();
        System.out.println("Knows load complete.");
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

    /*
     * map random generated UUIDs back to users/devices since the *Id values
     * will be turned into *real* IDs once the additions to the graph happen
     */
    public static void buildMaps() {
        for (User u : users) {
            userMap.put(u.getUserId(), u);
        }
        System.out.println("Created userMap");
        for (Device d : devices) {
            deviceMap.put(d.getDevId(), d);
        }
        System.out.println("Created deviceMap");
        System.out.println();
    }
    
    
    /*
     * provides user progress/performance updates on "operations"
     * this routine does not know (or care) what an operation is
     * re-used by all the threaded loading routines
     */
    private static class MonitorWorker implements Runnable {        

        public boolean go;
        
        public MonitorWorker() {
            go = true;
        }

        @Override
        public void run() {
            System.out.println("\t#\tops\tsince\ttime\tlatency\t\trate");
            System.out.println("\t-\t---\t-----\t----\t-------\t\t----");

            DecimalFormat df = new DecimalFormat("#.##");

            long startTime = System.currentTimeMillis();
            long last = startTime;
            long recent = 0;
            long n = 0;
            long threadId = Thread.currentThread().getId();
            do {
                try {
                    Thread.sleep(1000);                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
                }
                long now = System.currentTimeMillis();
                long d = now - last;
                if (((d >= 1000) && (operations.get() > 0)) || (!go)) {
                    n++;
                    int i = operations.get();
                    recent = i - recent;
                    double rate = (double) recent * 1000 / (double) d;
                    double latency = (double) d / (double) recent;
                    System.out.println("\t" + n + "\t" + i + "\t" + recent + "\t" + d + "\t" + df.format(latency) + "ms\t\t" + df.format(rate) + "/sec");
                    recent = i;
                    last = now;
                }
            } while (this.go);
        }
    }

    private static class UserAddWorker implements Runnable {
        public User u;

        public UserAddWorker(User u) { this.u = u; }

        @Override
        public void run() {
            u.setUserId(conn.putUser(u).getUserId());            
            operations.getAndIncrement();
        }
    }

    private static class DeviceAddWorker implements Runnable {
        private User u;
        private Device d;
        
        public DeviceAddWorker(User u, Device d) { this.u = u ; this.d = d; }
        
        @Override
        public void run() {
            try {
                d.setDevId(conn.putDevice(Integer.parseInt(u.getUserId()), d).getDevId());
                operations.getAndIncrement();
            } catch (Exception ex) {
                Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        
    }
    
    private static class UserKnowsWorker implements Runnable {
        private final User u;
        private final User v;
        private final int strength;
        
        public UserKnowsWorker(User u, User v, int strength) {
            this.u = u;
            this.v = v;
            this.strength = strength;
        }
        
        @Override
        public void run() {
            conn.userKnows(u, v, strength);
            operations.getAndIncrement();            
        }
    }
    
}
