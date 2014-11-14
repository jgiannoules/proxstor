package com.giannoules.proxstor.admin.testing;

import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.environmental.EnvironmentalDao;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidParameter;
import com.giannoules.proxstor.exception.LocationAlreadyNearbyLocation;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.nearby.NearbyDao;
import com.giannoules.proxstor.testing.generator.EnvironmentalGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationWorker implements Runnable {

    private Integer count;
    private final List<Location> locationPool;
    private final Random r;
    private final LocationGenerator lg;
    private final EnvironmentalGenerator eg;

    public LocationWorker(Integer count) {
        this.r = new Random();
        this.lg = new LocationGenerator(this.r);
        this.eg = new EnvironmentalGenerator(this.r);
        this.locationPool = new ArrayList<>();
        this.count = count;
    }

    @Override
    public void run() {
        Location l, m;
        Environmental e, f;
        Integer n;
        Set<Location> tmpLocations = new HashSet<>();
        while (count > 0) {

            // generate and add new location
            l = lg.genLocation();
            m = null;
            while (m == null) {
                m = LocationDao.instance.add(l);
                if (m == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(com.giannoules.proxstor.testing.report.LocationWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            l.setLocId(m.getLocId());

            // going to be nearby another location?
            if (r.nextBoolean() && locationPool.size() > 0) {
                n = r.nextInt(2) + 1;
                if (n > locationPool.size()) {
                    n = locationPool.size();
                }
                tmpLocations.clear();
                while (n > 0) {
                    tmpLocations.add(locationPool.get(r.nextInt(locationPool.size())));
                }
                for (Location x : tmpLocations) {
                    try {
                        do {
                        } while (NearbyDao.instance.addNearby(l.getLocId(), x.getLocId(), r.nextDouble() * 10000));
                    } catch (InvalidLocationId | LocationAlreadyNearbyLocation ex) {
                        Logger.getLogger(LocationWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            // randomly pop out a location
            if (!locationPool.isEmpty()) {
                locationPool.remove(r.nextInt(locationPool.size()));
            }

            e = eg.genEnvironmentals();

            f = null;
            while (f == null) {
                try {
                    f = EnvironmentalDao.instance.add(l.getLocId(), e);
                    if (f == null) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(com.giannoules.proxstor.testing.report.LocationWorker.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (InvalidParameter ex) {
                    Logger.getLogger(LocationWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            e.setEnvironmentalId(f.getEnvironmentalId());

            count--;
        }
    }
}
