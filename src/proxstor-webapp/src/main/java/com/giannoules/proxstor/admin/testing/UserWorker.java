package com.giannoules.proxstor.admin.testing;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.user.UserDao;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserWorker implements Runnable {

    private Integer count;
    private final Random r;
    private final UserGenerator ug;


    public UserWorker(UserGenerator ug, Integer count) {
        this.ug = ug;
        this.count = count;
        this.r = new Random();
    }

    @Override
    public void run() {
        User u, v;        
        while (count > 0) {                        
            u = ug.genUser();
            v = null;
            while (v == null) {
                v = UserDao.instance.add(u);
                if (v == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UserWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }                                  
            count--;
        }
    }
}
