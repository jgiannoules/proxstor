package com.giannoules.proxstor.testing.report;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.testing.loader.*;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import com.giannoules.proxstor.testing.generator.DeviceGenerator;
import com.giannoules.proxstor.testing.generator.EnvironmentalGenerator;
import com.giannoules.proxstor.testing.generator.LocationGenerator;
import com.giannoules.proxstor.testing.generator.UserGenerator;
import static com.giannoules.proxstor.testing.report.ReportStaticGenerator.conn;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationWorker implements Runnable {

    private Integer count;
    private final List<Location> locationPool;
    private final ProxStorConnector conn;
    private final AtomicInteger operations;
    private final LocationGenerator lg;
    private final Random r;
    private final List<String> locationIds;  
    private final List<String> environmentalIds;  
    private final EnvironmentalGenerator eg;


    public LocationWorker(LocationGenerator lg, EnvironmentalGenerator eg, String connectionString, AtomicInteger operations, Integer count, List locationIds, List environmentalIds) {
        this.lg = lg;
        this.conn = new ProxStorConnector(connectionString);
        this.operations = operations;
        this.locationPool = new ArrayList<>();
        this.count = count;
        this.r = new Random();
        this.locationIds = locationIds;
        this.environmentalIds = environmentalIds;
        this.eg = eg;
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
                m = conn.addLocation(l);
                if (m == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LocationWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            l.setLocId(m.getLocId());
                
            // going to be nearby another location?
            if (r.nextBoolean() && locationPool.size() > 0) {
                n = r.nextInt(2) + 1;
                if (n > locationPool.size())
                    n = locationPool.size();
                tmpLocations.clear();
                while (n > 0) {
                    tmpLocations.add(locationPool.get(r.nextInt(locationPool.size())));
                }
                for (Location x : tmpLocations) {
                    do {
                        
                    } while (conn.addLocationNearby(l.getLocId(), x.getLocId(), r.nextInt(10000)));
                }
            }
            
            // randomly pop out a location
            if (!locationPool.isEmpty()) {
                locationPool.remove(r.nextInt(locationPool.size()));
            }
            
            e = eg.genEnvironmentals();
           
            f = null;
            while (f == null) {
                f = conn.addEnvironmental(l.getLocId(), e);
                if (f == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LocationWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }        
            e.setEnvironmentalId(f.getEnvironmentalId());            
            
            locationIds.add(l.getLocId());
            environmentalIds.add(e.getEnvironmentalId());            
            
            operations.getAndIncrement();
            count--;
        }
    }
}
