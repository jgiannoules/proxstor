package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.connection.ProxStorConnector;
import com.giannoules.proxstor.api.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {

    static ProxStorConnector conn = new ProxStorConnector("http://localhost:8080/proxstor-webapp/api/");
    static Random random = new Random();
    
    /*
     * args[0] number of users
     * args[1] number of unique devices
     * args[2] number of locations
     * args[3] average number of sensors per location
     */
    public static void main(String[] args) throws Exception {
        
        for (int i = 0; i < args.length; i++) {
            System.out.println("argument " + i + ": " + args[i]);
        }
        
        if(args.length < 2) {
            System.out.println("command argument error");
            return;
        }
        
        Integer userCount = Integer.parseInt(args[0]);
        Integer uniqueDevices = Integer.parseInt(args[1]);
        
        UserGenerator ug = new UserGenerator();
        DeviceGenerator dg = new DeviceGenerator();
        
        System.out.println("ProxStor Static Content Generator");
        System.out.println("=================================");
        System.out.println("Goals:");
        System.out.println("\tNumber of Users: " + userCount);
        System.out.println("\tNumber of Unique Devices: " + uniqueDevices);
        System.out.println();
        
        
        System.out.print("Generating users...");
        long startTime = System.currentTimeMillis();
        List<User> users = new ArrayList<>();
        while (users.size() < userCount) {
            users.add(ug.genUser());
        }
        long endTime = System.currentTimeMillis();
        System.out.println("done. (" + (endTime - startTime) + " ms)" );
        
        System.out.print("Generating devices...");
        startTime = System.currentTimeMillis();
        List<Device> devices = new ArrayList<>();
        while (devices.size() < uniqueDevices) {
            devices.add(dg.genDevice());
        }
        endTime = System.currentTimeMillis();
        System.out.println("done. (" + (endTime - startTime) + " ms)" );
       
        int i = 0;
        System.out.print("Inserting users and devices...");
        startTime = System.currentTimeMillis();
        for (User u : users) {
            Device d = devices.get(random.nextInt(devices.size()));
            u = conn.putUser(u);
            conn.putDevice(Integer.parseInt(u.getUserId()), d);
            i++;
            if (i % 10 == 0) {
                System.out.println(" " + i + " ");
                System.out.flush();
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("done. (" + (endTime - startTime) + " ms)" );
    }
    
}
