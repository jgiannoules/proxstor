package com.giannoules.proxstor.testing.generator;

import com.giannoules.proxstor.testing.generator.DeviceGenerator;
import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.api.SensorType;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.testing.ReaderWriter;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/*
 * Generator - create series of persistent JSON files intended to be read by
 * the Loader. The goal is to recreate an exact replica of the static data 
 * in multiple database instances/configurations/ProxStor revisions to allow
 * clean apples-to-apples comparisons.
 *
 * Note that the use of all command line options (including Seed) will recreate 
 * the same dataset and files for a specific revision of the proxstor-testing and 
 * proxstor-connection projects.
 *
 * The utility is for testing only. Not robust. Does not fail gracefully.
 *
 * @TODO start writing to file before all structures generated to stop blowing
 * out the heap on large datasets
 */
public class Generator {

    static Random random;
    static List<User> users;
    static List<Device> devices;
    static List<Location> locations;
    static List<Sensor> sensors;

    static Integer userCount;
    static Integer uniqueDevices;
    static Integer avgDeviceCount;
    static Integer locationCount;
    static Integer avgSensorCount;
    static Long randomSeed;

    /*
     * args[0] number of users
     * args[1] number of unique devices
     * args[2] average number of devices per user
     * args[3] number of locations
     * args[4] average number of sensors per location
     * args[5] random seed (optional)
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 4) {
            System.out.println("insufficient command arguments");
            return;
        }

        random = new Random();

        userCount = Integer.parseInt(args[0]);
        uniqueDevices = Integer.parseInt(args[1]);
        avgDeviceCount = Integer.parseInt(args[2]);
        locationCount = Integer.parseInt(args[3]);
        avgSensorCount = Integer.parseInt(args[4]);
        if (args.length > 5) {
            randomSeed = Long.parseLong(args[5]);
            random.setSeed(randomSeed);
        }

        System.out.println("ProxStor Static Content Generator");
        System.out.println("=================================");
        System.out.println("Goals:");
        System.out.println("\tNumber of Users: " + userCount);
        System.out.println("\tNumber of Unique Devices: " + uniqueDevices);
        System.out.println("\tAverage Number of Devices per User: " + avgDeviceCount);
        System.out.println("\tNumber of Locations: " + locationCount);
        System.out.println("\tAverage Number of Sensors per Location: " + avgSensorCount);
        if (randomSeed != null) {
            System.out.println("\tRandom Seed: " + randomSeed);
        }
        System.out.println();

        genUsers();
        genKnows();
        genDevices();
        assignDevices();        
        genLocations();        
        genAndAssignSensors();
        assignWithinLocations();
        assignNearbyLocations();

        String dir = createDir();

        writeToFile(dir, "users.json", users);
        writeToFile(dir, "devices.json", devices);
        writeToFile(dir, "locations.json", locations);
        writeToFile(dir, "sensors.json", sensors);

        System.out.println("\nFiles saved to:");
        System.out.println("\t" + new File(".").getCanonicalPath() + "/" + dir + "/");        
        System.out.println();
    }

    /*
     * use UserGenerator() to continually generate random users and add them
     * to the users list. Since user generation can be time consuming
     * print a dot "." every 10% through the process.
     */
    public static void genUsers() {
        UserGenerator ug = new UserGenerator(random);
        System.out.print("Generating users...");
        long startTime = System.currentTimeMillis();
        users = new ArrayList<>();
        int tenth = userCount / 10;
        if (tenth == 0) {
            tenth = 1;
        }
        while (users.size() < userCount) {
            users.add(ug.genUser());
            if (users.size() % tenth == 10) {
                System.out.print(".");
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("done. (" + (endTime - startTime) + " ms)");
    }
    
    public static void genKnows() {
        System.out.print("Establishing knows between users...");
        long startTime = System.currentTimeMillis();
        /*
         * 1. randomly select two users
         * 2. if they already know each other, go back to #1
         * 3. establish getKnowsStrength from a -> b
         * 4. with 90% probability, estaliblish b -> a
         * 5. repeat until knowsCount >= 3x userCount
         */
        long knowsCount = 0;
        long reciprocated = 0;
        while (knowsCount < (users.size() * 3)) {
            User userA = users.get(random.nextInt(users.size()));
            User userB = users.get(random.nextInt(users.size()));
            if (userA.getKnowsStrength(userB.getUserId()) == null) {
                int strength = random.nextInt(101);
                userA.addKnows(userB.getUserId(), strength);
                knowsCount++;
                if (random.nextInt(10) != 0) {
                    /*
                     * reciprocal relationship within 20%
                     */
                    reciprocated++;
                    strength += random.nextInt(41) - 20; // [0 - 40] - 20
                    if (strength < 0) {
                        strength = 0;
                    }
                    if (strength > 100) {
                        strength = 100;
                    }
                    userB.addKnows(userA.getUserId(), strength);
                    knowsCount++;
                }
            }
        }
       long endTime = System.currentTimeMillis();       
       System.out.print("done. (" + (endTime - startTime) + " ms)");
       System.out.println(" [knows: " + knowsCount + " reciprocated: " + reciprocated * 2 + "]");
    }

    /*
     * use DeviceGenerator() to continually generate random devices and add them
     * to the devices list. 
     */
    public static void genDevices() {
        DeviceGenerator dg = new DeviceGenerator(random);
        System.out.print("Generating devices...");
        long startTime = System.currentTimeMillis();
        devices = new ArrayList<>();
        while (devices.size() < uniqueDevices) {
            devices.add(dg.genDevice());
        }
        long endTime = System.currentTimeMillis();
        System.out.println("done. (" + (endTime - startTime) + " ms)");
    }

    /*
     * for each user decide whether the user will have a typical number of 
     * devices (70% population), or atypical. 
     */
    public static void assignDevices() {
        System.out.print("Assigning devices...");
        long startTime = System.currentTimeMillis();
        long assignedCount = 0;
        int maxAssigned = 0;
        int minAssigned = Integer.MAX_VALUE;
        /*
         * each user is assigned an average of averageDeviceCount devices
         * selected randomly from the devices List
         */
        for (User u : users) {
            /*
             * each user has a 70% change of being dead-on average in the device
             * count; otherwise chose device count from 1 to 2x avg count.
             */
            boolean average = (random.nextInt(10) <= 6);
            int numDevices;
            if (average) {
                numDevices = avgDeviceCount;
            } else {
                numDevices = 1 + random.nextInt(avgDeviceCount * random.nextInt(5) + 1);
            }
            // some simple statistics collection
            assignedCount += numDevices;
            if (numDevices > maxAssigned) {
                maxAssigned = numDevices;
            }
            if (numDevices < minAssigned) {
                minAssigned = numDevices;
            }
            while (numDevices > 0) {
                Device d = devices.get(random.nextInt(devices.size()));
                if (!u.hasDevice(d)) {
                    u.addDevice(d);
                    numDevices--;
                }
            }
        }
        float avg = assignedCount / users.size();
        long endTime = System.currentTimeMillis();
        System.out.print("done. (" + (endTime - startTime) + " ms)");
        System.out.println(" [min/max/avg " + minAssigned + "/" + maxAssigned + "/" + avg + "]");
    }

    /*
     * use LocationGenerator() to continually generate random locations and add 
     * them to the locations list. Since location generation can be time consuming
     * print a dot "." every 10% through the process.
     */
    public static void genLocations() {
        LocationGenerator lg = new LocationGenerator(random);
        System.out.print("Generating locations...");
        long startTime = System.currentTimeMillis();
        locations = new ArrayList<>();
        int tenth = locationCount / 10;
        while (locations.size() < locationCount) {
            locations.add(lg.genLocation());
            if (locations.size() % tenth == 10) {
                System.out.print(".");
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("done. (" + (endTime - startTime) + " ms)");
    }

    /*
     * wrapper to create new directory
     */
    public static String createDir() {                
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        String dir = dateFormat.format(date);
        new File(dir).mkdir();        
        return dir;
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
     * use SensorGenerator() to continually generate random sensors and add them
     * to the sensors list. 
     */
    public static void genAndAssignSensors() {
        System.out.print("Generating and assigning sensors...");
        sensors = new ArrayList<>();
        SensorGenerator sg = new SensorGenerator(random);
        long startTime = System.currentTimeMillis();
        long tenth = locationCount / 10;
        long passCount = 0; 
        long assignedCount = 0;
        int maxAssigned = 0;
        int minAssigned = Integer.MAX_VALUE;
        /*
         * each location is assigned an average of averageSensorCount sensors
         * generated uniquely from one SensorGenerator instance
         */
        outer: for (Location l : locations) {
            /*
             * each location has a 70% change of being dead-on average in the
             * sensor count; otherwise chose sensor count from 1 to 5x avg count.
             */
            boolean average = (random.nextInt(10) <= 6);
            int numSensors;
            if (average) {
                numSensors = avgSensorCount;
            } else {
                numSensors = 1 + random.nextInt(avgSensorCount * random.nextInt(5) + 1);
            }
            // some simple statistics collection
            assignedCount += numSensors;
            if (numSensors > maxAssigned) {
                maxAssigned = numSensors;
            }
            if (numSensors < minAssigned) {
                minAssigned = numSensors;
            }
            while (numSensors > 0) {
                Sensor s = sg.genSensor();
                if (s.getType() == SensorType.UNKNOWN) {
                    System.out.println("How!");
                }
                if (s != null) {                                    
                    sensors.add(s);
                    l.addSensor(s);
                    numSensors--;   
                }
            }            
            if (passCount++ % tenth == 10) {
                System.out.print(".");
            }
        }
        float avg = assignedCount / users.size();
        long endTime = System.currentTimeMillis();
        System.out.print("done. (" + (endTime - startTime) + " ms)");
        System.out.println(" [min/max/avg " + minAssigned + "/" + maxAssigned + "/" + avg + "]");
    }
    
    /*
     * 
     */
    public static void assignWithinLocations() {
       System.out.print("Assigning locations within locations...");
       long startTime = System.currentTimeMillis();       
       long totLength = 0;
       long numPasses = 0;
       long numWithin = 0;
       long shortest = Long.MAX_VALUE;
       long longest = 0;
       List<Location> unassigned = new ArrayList<>(locations);
       /*
        * target ~85% of locations as being within one or more locations
        */
       while (unassigned.size() > (locations.size() * 0.15)) {
           int length = random.nextInt(9) + 1;
           if (length > longest) {
               longest = length;
           }
           if (length < shortest) {
               shortest = length;
           }
           numPasses++;
           totLength+=length;
           int i = random.nextInt(unassigned.size());
           Location l = unassigned.get(i);
           unassigned.remove(i);
           while (length > 0) {
               int j = random.nextInt(unassigned.size());
               Location m = unassigned.get(j);
               numWithin++;
               l.addWithin(m.getLocId());
               unassigned.remove(j);
               length--;
               l = m;
           }
       }
       long endTime = System.currentTimeMillis();
       System.out.print("done. (" + (endTime - startTime) + " ms)");
       float avg = totLength / numPasses;
       System.out.println(" [min/max/avg/total " + shortest + "/" + longest + "/" + avg + "/" + numWithin + "]");
    }
    
    /*
     * 
     */
    public static void assignNearbyLocations() {
       System.out.print("Assigning nearby locations...");
       long startTime = System.currentTimeMillis();
       long numLocations = 0;
       long totNearby = 0;
       long numPasses = 0;
       long numNearby = 0;
       long most = Long.MAX_VALUE;
       long least = 0;
       List<Location> unassigned = new ArrayList<>(locations);
       /*
        * target ~99% of locations as being nearby one or more locations
        */
       while (unassigned.size() > (locations.size() * 0.01)) {
           int count = random.nextInt(9) + 1;
           if (count > least) {
               least = count;
           }
           if (count < most) {
               most = count;
           }
           numPasses++;
           totNearby+=count;
           int i = random.nextInt(unassigned.size());
           Location l = unassigned.get(i);
           unassigned.remove(i);
           while (count > 0) {
               int j = random.nextInt(unassigned.size());
               Location m = unassigned.get(j);
               numNearby++;
               int distance = random.nextInt(996) + 5;
               l.addNearby(m.getLocId(), distance);
               unassigned.remove(j);
               count--;               
           }
       }
       long endTime = System.currentTimeMillis();
       System.out.print("done. (" + (endTime - startTime) + " ms)");
       float avg = numNearby / numPasses;
       System.out.println(" [min/max/avg/total " + most + "/" + least + "/" + avg + "/" + numNearby + "]");
    }
}
