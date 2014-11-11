package com.giannoules.proxstor.admin.testing;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.user.UserDao;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationWorker implements Runnable {

    private Integer count;
    private final Random r;
    private final LocationGenerator ug;


    public LocationWorker(LocationGenerator lg, Integer count) {
        this.ug = lg;
        this.count = count;
        this.r = new Random();
    }

    @Override
    public void run() {
        Location l, m;        
        while (count > 0) {                        
            l = ug.genLocation();
            m = null;
            while (m == null) {
                m = LocationDao.instance.add(l);
                if (m == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LocationWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }                                  
            count--;
        }
    }
}
