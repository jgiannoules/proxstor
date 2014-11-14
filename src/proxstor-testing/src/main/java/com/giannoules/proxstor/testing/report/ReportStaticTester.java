package com.giannoules.proxstor.testing.report;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Query;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportStaticTester {
   
    static Random random;
    static Integer iterations;
    
    static String connectionString;
    static ProxStorConnector conn;

    static List<User> randomUsers;
    static List<Device> randomDevice;    
    static List<Location> randomLocations;
    static List<Environmental> randomEnvironmentals;
    
    static Map<String, String> checkins;

    static Integer userCount;
    static Integer locationCount;
    static Long randomSeed;

    /*
     * args[0] number of iterations
     * args[1] connection string     
     * args[2] random (optional)
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("insufficient command arguments");
            return;
        }

        random = new Random();     
        checkins = new HashMap<>();
        
        iterations = Integer.parseInt(args[0]);
        connectionString = args[1];
        
         if (args.length > 2) {
            randomSeed = Long.parseLong(args[2]);
            random.setSeed(randomSeed);
        }
        
        System.out.println("ProxStor Static Tester");
        System.out.println("======================");
        System.out.println("\tIterations: " + iterations);
        System.out.println("\tConnection String: " + connectionString);        
        if (randomSeed != null) {
            System.out.println("\tRandom Seed: " + randomSeed);
        }
        System.out.println();
        
        System.out.println("Please confirm the database is already populated.");
        pressEnter();
      
        conn = new ProxStorConnector(connectionString);
        
        getRandomUsers(iterations * 10);
        getRandomDevices(iterations * 10);
        getRandomLocations(iterations * 10);
        getRandomEnvironmentals(iterations * 10);

        stopStart();
        
        randomCheckin(iterations);
        stopStart();
        
        getCurrentLocation(iterations);
        stopStart();
        
        withinLocation(iterations);
        stopStart();
        
        System.out.println("Checking in more users before next test...");
        checkins = new HashMap<>();
        randomCheckin(iterations * 5);
        stopStart();
        
        randomCheckout(iterations);
    }
    
    private static void getRandomUsers(int count) {
        System.out.print("Retrieving " + count + " random Users...");
        randomUsers = new ArrayList(conn.getTestingRandomUsers(iterations));
        System.out.println("done.");
    }
    
    private static void getRandomDevices(int count) {
        System.out.print("Retrieving " + count + " random Devices...");
        randomDevice = new ArrayList(conn.getTestingRandomDevices(iterations));
        System.out.println("done.");
    }
    
    private static void getRandomEnvironmentals(int count) {
        System.out.print("Retrieving " + count + " random Environmentals...");
        randomEnvironmentals = new ArrayList(conn.getTestingRandomEnvironmentals(iterations));
        System.out.println("done.");
    }
    
    private static void getRandomLocations(int count) {
        System.out.print("Retrieving " + count + " random Locations...");
        randomLocations = new ArrayList(conn.getTestingRandomLocations(iterations));
        System.out.println("done.");
    }
    
    private static void pressEnter() {
        System.out.println("Press Enter to continue...");  
        try{
            System.in.read();
        }  
        catch(Exception e){
        }
    }
    
    private static void stopStart() {
        conn = null;
        System.out.println("\nStop and start ProxStor and database...");
        pressEnter();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ReportStaticTester.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("continuing...\n");
    }
    
    private static void getCurrentLocation(int count) {
        conn = new ProxStorConnector(connectionString);
        List<Long> times = new ArrayList<>();
        Query q;
        long total = 0;
        long big = 0;
        long small = Long.MAX_VALUE;
        for (int i = 0; i < count; i++) {            
            User u = randomUsers.get(random.nextInt(randomUsers.size()));         
                        
            q = new Query();
            q.setUserId(u.getUserId());
            
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
        double d = total / count;
        System.out.println("getCurrentLocation Results");
        System.out.println("\t" + count + " current location checks");
        System.out.println("\tmin: " + small + "ms");
        System.out.println("\tmax: " + big + "ms");
        System.out.println("\tavg: " + d + "ms");        
    }
    
    private static void randomCheckout(int count) {
        conn = new ProxStorConnector(connectionString);
        List<Long> times = new ArrayList<>();
        long total = 0;
        long n = 0;
        long big = 0;
        long small = Long.MAX_VALUE;        
        Iterator it = checkins.keySet().iterator();
        for (String d : checkins.values()) {
            String devId = d;
            String envId = checkins.get(d);

            long startTime = System.currentTimeMillis();
            conn.deviceUndetectsEnvironmentalId(devId, envId);
            long endTime = System.currentTimeMillis();            

            long duration = endTime - startTime;
            total += duration;
            if (duration < small)
                small = duration;
            if (duration > big)
                big = duration;
            times.add(duration);
            n++;
        }
        System.out.println("done.");
        double d;
        if (n > 0)
            d = total / n;
        else
            d = 0;
        System.out.println("randomCheckout Results");
        System.out.println("\t" + n + " check-outs");
        System.out.println("\tmin: " + small + "ms");
        System.out.println("\tmax: " + big + "ms");
        System.out.println("\tavg: " + d + "ms");
    }
    
    private static void withinLocation(int count) {
        conn = new ProxStorConnector(connectionString);
        List<Long> times = new ArrayList<>();
        Query q;
        long total = 0;
        long big = 0;
        long small = Long.MAX_VALUE;
        for (int i = 0; i < count; i++) {            
            Location l = randomLocations.get(random.nextInt(randomLocations.size()));           
            User u = randomUsers.get(random.nextInt(randomUsers.size()));            
                        
            q = new Query();
            q.setUserId(u.getUserId());
            q.setLocationId(l.getLocId());
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
        double d = total / count;
        System.out.println("withinLocation Results");
        System.out.println("\t" + count + " within location checks");
        System.out.println("\tmin: " + small + "ms");
        System.out.println("\tmax: " + big + "ms");
        System.out.println("\tavg: " + d + "ms");
    }
    
    private static void randomCheckin(int count) {
        conn = new ProxStorConnector(connectionString);
        List<Long> times = new ArrayList<>();
        long total = 0;
        long big = 0;
        long small = Long.MAX_VALUE;
        for (int i = 0; i < count; i++) {            
            Device d = randomDevice.get(random.nextInt(randomDevice.size()));
            Environmental e = randomEnvironmentals.get(random.nextInt(randomEnvironmentals.size()));
            
            long startTime = System.currentTimeMillis();
            conn.deviceDetectsEnvironmentalId(d.getDevId(), e.getEnvironmentalId());
            long endTime = System.currentTimeMillis();            
            
            long duration = endTime - startTime;
            total += duration;
            if (duration < small)
                small = duration;
            if (duration > big)
                big = duration;
            times.add(duration);         
            
            checkins.put(d.getDevId(), e.getEnvironmentalId());
        }
        
        System.out.println("done.");
        double d = total / count;
        System.out.println("randomCheckin Results");
        System.out.println("\t" + count + " check-ins");
        System.out.println("\tmin: " + small + "ms");
        System.out.println("\tmax: " + big + "ms");
        System.out.println("\tavg: " + d + "ms");
    }
   

}
