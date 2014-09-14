package com.giannoules.proxstor.testing.generator;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.LocationType;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class LocationGenerator {
    /*
     * uniqueness of location tied to description
     */
    private final Set<String> descriptions;
    private final Random random;
    
    public LocationGenerator(Random random) {
        this.descriptions = new HashSet<>();
        this.random = random;
    }
    
    public Location genLocation() {
        Location l = new Location();
        String s;
        do {
            String base = Integer.toString(random.nextInt());
            switch (random.nextInt(5)) {
                case 0: s = "Lane";
                        break;
                case 1: s = "Street";
                        break;
                case 2: s = "Avenue";
                        break;
                case 3: s = "Boulevard";
                        break;
                case 4: s = "Court";
                        break;
                default: s = "Not Possible";
                        break;
            }
            l.setAddress(base + " Aimless " + s);
            l.setType(LocationType.values()[random.nextInt(LocationType.values().length)]);
            /*
             * Latitude/Longitude random for now. No bearing on whether it makes sense or if
             * a location can possibly actually be within another location.
             */
            l.setLatitude(random.nextDouble());
            l.setLongitude(random.nextDouble());
            l.setDescription(l.getAddress() + " at " + l.getLatitude() + ", " + l.getLatitude());
            /*
             * this a testing-specific misuse of the locId field needed to assist
             * in trackikng a specific Location. When the locations are actually 
             * being inserted into ProxStor the real UUID will be generated by 
             * the tool and locId field will be  ignored and assigned on the 
             * webapi side of ProxStor
             */
            l.setLocId(UUID.randomUUID().toString());
        } while (descriptions.contains(l.getDescription()));
        return l;
    }
}
