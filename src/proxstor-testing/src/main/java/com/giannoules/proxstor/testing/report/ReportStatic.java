package com.giannoules.proxstor.testing.report;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Query;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import com.giannoules.proxstor.testing.generator.DeviceGenerator;
import com.giannoules.proxstor.testing.generator.EnvironmentalGenerator;
import static com.giannoules.proxstor.testing.generator.Generator.genKnows;
import static com.giannoules.proxstor.testing.generator.Generator.genUsers;
import com.giannoules.proxstor.testing.generator.LocationGenerator;
import com.giannoules.proxstor.testing.generator.UserGenerator;
import com.giannoules.proxstor.testing.loader.DeviceAddWorker;
import com.giannoules.proxstor.testing.loader.Loader;
import com.giannoules.proxstor.testing.loader.MonitorWorker;
import com.giannoules.proxstor.testing.loader.UserAddWorker;
import static java.lang.Math.random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportStatic {

    final static Integer THREAD_COUNT = 32;
    
    final static Integer LOOP_COUNT = 1000;

    static Random random;

    static MonitorWorker mw;
    static Thread monitorThread;
    
    static String connectionString;
    static ProxStorConnector conn;

    static List<String> userIds;
    static List<String> deviceIds;    
    static List<String> locationIds;
    static List<String> environmentalIds;
    
    static Map<String, String> checkins;

    static Integer userCount;
    static Integer locationCount;
    static Long randomSeed;
    
    static AtomicInteger operations = new AtomicInteger();


    /*
     * args[0] number of users
     * args[1] number of locations     
     * args[2] connection String
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.out.println("insufficient command arguments");
            return;
        }

        random = new Random();        
        
        userCount = Integer.parseInt(args[0]);
        locationCount = Integer.parseInt(args[1]);
        connectionString = args[2];
        
         if (args.length > 3) {
            randomSeed = Long.parseLong(args[3]);
            random.setSeed(randomSeed);
        }
        
        System.out.println("ProxStor Report Testing Generator");
        System.out.println("=================================");
        System.out.println("Goals:");
        System.out.println("\tNumber of Users: " + userCount);
        System.out.println("\tNumber of Locations: " + locationCount);
        System.out.println("\tConnection String: " + connectionString);
        
        if (randomSeed != null) {
            System.out.println("\tRandom Seed: " + randomSeed);
        }
        System.out.println();
        
        
        userIds = Collections.synchronizedList(new ArrayList<String>());
        deviceIds = Collections.synchronizedList(new ArrayList<String>());
        locationIds = Collections.synchronizedList(new ArrayList<String>());
        environmentalIds = Collections.synchronizedList(new ArrayList<String>());
        
        
        checkins = new HashMap<>();
        
        generateAddUsers();
        generateAddLocations();
        
        System.out.println("Press Enter to continue");  
        try{System.in.read();}  
        catch(Exception e){}  
        System.out.print("Sleeping 5000 ms...");
        Thread.sleep(5000);
        System.out.println("done");        
        randomCheckin();
        
        System.out.println("Press Enter to continue");  
        try{System.in.read();}  
        catch(Exception e){}        
        System.out.print("Sleeping 5000 ms...");
        Thread.sleep(5000);
        System.out.println("done");        
        getCurrentLocation();
        
        System.out.println("Press Enter to continue");  
        try{System.in.read();}  
        catch(Exception e){}  
        System.out.print("Sleeping 5000 ms...");
        Thread.sleep(5000);
        System.out.println("done");                
        withinLocation();
        
        System.out.println("Press Enter to continue");  
        try{System.in.read();}  
        catch(Exception e){}  
        System.out.print("Sleeping 5000 ms...");
        Thread.sleep(5000);
        System.out.println("done");                
        randomCheckout();  
        
    }
    
    private static void getCurrentLocation() {
        conn = new ProxStorConnector(connectionString);
        List<Long> times = new ArrayList<>();
        Query q;
        long total = 0;
        long big = 0;
        long small = Long.MAX_VALUE;
        for (int i = 0; i < LOOP_COUNT; i++) {            
            String userId = userIds.get(random.nextInt(userIds.size()));            
                        
            q = new Query();
            q.setUserId(userId);
            
            long startTime = System.currentTimeMillis();
            conn.query(q);
            long endTime = System.currentTimeMillis();            
            
            long duration = endTime - startTime;
            total += duration;
            if (duration < small)
                small = duration;
            if (duration > big)
                big = duration;
            times.add(duration);
        }
        System.out.println("done.");
        double d = total / LOOP_COUNT;
        System.out.println("getCurrentLocation Results");
        System.out.println("\t" + LOOP_COUNT + " current location checks");
        System.out.println("\tmin: " + small + "ms");
        System.out.println("\tmax: " + big + "ms");
        System.out.println("\tavg: " + d + "ms");        
    }
    
    private static void randomCheckout() {
        conn = new ProxStorConnector(connectionString);
        List<Long> times = new ArrayList<>();
        Query q;
        long total = 0;
        long count = 0;
        long big = 0;
        long small = Long.MAX_VALUE;        
        for (String u : checkins.values()) {
            String userId = u;
            String locId = checkins.get(u);       

            long startTime = System.currentTimeMillis();
            conn.userCheckoutLocation(userId, locId);
            long endTime = System.currentTimeMillis();            

            long duration = endTime - startTime;
            total += duration;
            if (duration < small)
                small = duration;
            if (duration > big)
                big = duration;
            times.add(duration);
            count++;
        }
        System.out.println("done.");
        double d = total / count;
        System.out.println("randomCheckout Results");
        System.out.println("\t" + count + " check-outs");
        System.out.println("\tmin: " + small + "ms");
        System.out.println("\tmax: " + big + "ms");
        System.out.println("\tavg: " + d + "ms");
    }
    
    private static void withinLocation() {
        conn = new ProxStorConnector(connectionString);
        List<Long> times = new ArrayList<>();
        Query q;
        long total = 0;
        long big = 0;
        long small = Long.MAX_VALUE;
        for (int i = 0; i < LOOP_COUNT; i++) {            
            String locId = locationIds.get(random.nextInt(locationIds.size()));            
            String userId = userIds.get(random.nextInt(userIds.size()));            
                        
            q = new Query();
            q.setUserId(userId);
            q.setLocationId(locId);
            q.setStrength(0);
            
            long startTime = System.currentTimeMillis();
            conn.query(q);
            long endTime = System.currentTimeMillis();            
            
            long duration = endTime - startTime;
            total += duration;
            if (duration < small)
                small = duration;
            if (duration > big)
                big = duration;
            times.add(duration);
        }
        System.out.println("done.");
        double d = total / LOOP_COUNT;
        System.out.println("withinLocation Results");
        System.out.println("\t" + LOOP_COUNT + " within location checks");
        System.out.println("\tmin: " + small + "ms");
        System.out.println("\tmax: " + big + "ms");
        System.out.println("\tavg: " + d + "ms");
    }
    
    private static void randomCheckin() {
        conn = new ProxStorConnector(connectionString);
        List<Long> times = new ArrayList<>();
        long total = 0;
        long big = 0;
        long small = Long.MAX_VALUE;
        for (int i = 0; i < LOOP_COUNT; i++) {            
            String userId = userIds.get(random.nextInt(userIds.size()));
            String locationId = locationIds.get(random.nextInt(locationIds.size()));
            
            long startTime = System.currentTimeMillis();
            conn.userCheckinLocation(userId, locationId);
            long endTime = System.currentTimeMillis();            
            
            long duration = endTime - startTime;
            total += duration;
            if (duration < small)
                small = duration;
            if (duration > big)
                big = duration;
            times.add(duration);
            
            checkins.put(userId, locationId);
        }
        System.out.println("done.");
        double d = total / LOOP_COUNT;
        System.out.println("randomCheckin Results");
        System.out.println("\t" + LOOP_COUNT + " check-ins");
        System.out.println("\tmin: " + small + "ms");
        System.out.println("\tmax: " + big + "ms");
        System.out.println("\tavg: " + d + "ms");
    }
   
    private static void generateAddUsers() {  
        UserGenerator ug = new UserGenerator(random);
        DeviceGenerator dg = new DeviceGenerator(random);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger counter = startMonitoring();
        for (int i = 0; i < THREAD_COUNT; i++) {
            Runnable worker = new UserWorker(ug, dg, connectionString, counter, userCount / THREAD_COUNT, userIds, deviceIds);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        stopMontoring();
        System.out.println("User load complete.");
        System.out.println();
    }
    
    private static void generateAddLocations() {  
        LocationGenerator lg = new LocationGenerator(random);
        EnvironmentalGenerator eg = new EnvironmentalGenerator(random);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger counter = startMonitoring();
        for (int i = 0; i < THREAD_COUNT; i++) {
            Runnable worker = new LocationWorker(lg, eg, connectionString, counter, locationCount / THREAD_COUNT, locationIds, environmentalIds);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        stopMontoring();
        System.out.println("Location load complete.");
        System.out.println();
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
 
}
