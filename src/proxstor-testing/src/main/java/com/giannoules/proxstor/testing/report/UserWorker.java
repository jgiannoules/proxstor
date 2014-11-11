package com.giannoules.proxstor.testing.report;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import com.giannoules.proxstor.testing.generator.DeviceGenerator;
import com.giannoules.proxstor.testing.generator.UserGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserWorker implements Runnable {

    private Integer count;
    private final List<User> userPool;
    private final ProxStorConnector conn;
    private final AtomicInteger operations;
    private final UserGenerator ug;
    private final Random r;
    private final List<String> deviceIds;  
    private final List<String> userIds;  
    private final DeviceGenerator dg;


    public UserWorker(UserGenerator ug, DeviceGenerator dg, String connectionString, AtomicInteger operations, Integer count, List userIds, List deviceIds) {
        this.ug = ug;
        this.conn = new ProxStorConnector(connectionString);
        this.operations = operations;
        this.userPool = new ArrayList<>();
        this.count = count;
        this.r = new Random();
        this.deviceIds = deviceIds;
        this.userIds = userIds;
        this.dg = dg;
    }

    @Override
    public void run() {
        User u, v;
        Device d, e;
        Integer n;
        Set<User> tmpUsers = new HashSet<>();
        while (count > 0) {
            
            // generate and add new user
            u = ug.genUser();
            v = null;
            while (v == null) {
                v = conn.addUser(u);
                if (v == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UserWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            u.setUserId(v.getUserId());
                
            // going to have a friend, or two, or more?
            if (r.nextBoolean() && userPool.size() > 0) {
                n = r.nextInt(2) + 1;
                if (n > userPool.size())
                    n = userPool.size();
                tmpUsers.clear();
                while (n > 0) {
                    tmpUsers.add(userPool.get(r.nextInt(userPool.size())));
                }
                for (User x : tmpUsers) {
                    do {
                        
                    } while (conn.addUserKnows(u, x, r.nextInt(100)+1));
                }
            }
            
            // randomly pop out a dude
            if (!userPool.isEmpty()) {
                userPool.remove(r.nextInt(userPool.size()));
            }
            
            d = dg.genDevice();
           
            e = null;
            while (e == null) {
                e = conn.addDevice(u.getUserId(), d);
                if (e == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UserWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }          
            d.setDevId(e.getDevId());
            
            userIds.add(u.getUserId());
            deviceIds.add(d.getDevId());
            
            operations.getAndIncrement();
            count--;
        }
    }
}
