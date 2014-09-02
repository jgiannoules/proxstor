package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.api.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player {
    
    static List<User> users;    
    static List<Device> devices;
    static List<Location> locations;
    static List<Sensor> sensors;

    public static void main(String[] args) {
        
        /*
        if (args.length < 1) {
            System.out.println("must provide directory");
            return;
        }
        
        String dir = args[0];
        */
        String dir = "20140901_211016";
        
        users = readFromFile(dir, "users.json", User.class);
        devices = readFromFile(dir, "devices.json", Device.class);
        //locations = readFromFile(dir, "locations.json", Location.class);
        
        //System.out.println(users);
        //System.out.println(devices);
        
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
    
}
