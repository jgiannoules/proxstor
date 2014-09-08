package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


/*
 * repeatedly select a random user id, and perform the following:
 *  - retrieve the user details
 *  - retrieve the user's devices
 *  - select a random knows strength 0 - 100, retrieve all the users in the range
 */
public class UserRetrievalWorker implements Runnable {
    
    private final ProxStorConnector conn;
    private final List<String> userIds;
    
    private final Random random;
    public boolean running;
    
    StressorMonitorWorker smw;
    
    public UserRetrievalWorker(ProxStorConnector conn, List<String> userIds, StressorMonitorWorker smw) {
        this.conn = conn;
        this.userIds = userIds;
        this.smw = smw;
        random = new Random();
        running = true;
    }
    
    @Override
    public void run() {
        int strength;
        String userId;        
        User u;
        Collection<Device> devices;
        Collection<User> users;
        do {
           //System.out.println("A");
           userId = userIds.get(random.nextInt(userIds.size()));
           strength = random.nextInt(101);
           u = conn.getUser(Integer.parseInt(userId));
           //System.out.println("B");
           smw.usersProcessed.getAndIncrement();
           devices = conn.getDevices(Integer.parseInt(u.getUserId()));
           //System.out.println("C");
           smw.deviceCount.addAndGet(devices.size());
           users = conn.getKnows(Integer.parseInt(u.getUserId()), strength);
           //System.out.println("");
           smw.knownUsers.addAndGet(users.size());
           //System.out.println(usersProcessed.get() + " " + deviceCount.get() + " " + knownUsers.get());
        } while (running);
    }
    
}
