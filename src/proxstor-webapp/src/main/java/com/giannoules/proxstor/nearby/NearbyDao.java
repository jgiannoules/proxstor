package com.giannoules.proxstor.nearby;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidModel;
import com.giannoules.proxstor.exception.LocationAlreadyNearbyLocation;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.location.LocationDao;
import static com.tinkerpop.blueprints.Compare.GREATER_THAN_EQUAL;
import static com.tinkerpop.blueprints.Compare.LESS_THAN_EQUAL;
import static com.tinkerpop.blueprints.Direction.BOTH;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum NearbyDao {
    
    instance;
    
    /**
     * Returns all locations with nearby relationship to locId results
     * controlled by:
     * <ul>
     * <li>with distance <= distanceVal <li>results are limited to a max of
     * limit
     * </ul>
     *
     * @param locId
     * @param distanceVal
     * @param limit
     *
     * @return Collection of Location objects matching criteria, or null if no
     * matches found
     *
     * @throws InvalidLocationId If the locID is invalid
     */
    public Collection<Location> getLocationsNearby(String locId, Double distanceVal) throws InvalidLocationId {
        Location l = LocationDao.instance.get(locId);
        if (distanceVal != null) {
            List<Location> nearby = new ArrayList<>();
            try {
                GraphQuery gq = ProxStorGraph.instance._query();
                gq.has("_type", "location");
                gq = queryDistanceBoundingBox(gq, l.getLatitude(), l.getLongitude(), distanceVal);
                for (Vertex v : gq.vertices()) {
                    nearby.add(LocationDao.instance.get(v));
                }
            } catch (ProxStorGraphDatabaseNotRunningException ex) {
                Logger.getLogger(NearbyDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return nearby;
        }
        return null;
    }

    /**
     * Establish a nearby relationship between two locations with distance
     *
     * @param locIdA String representation of location establishing the 'nearby' relationship
     * @param locIdB String representation of location receiving the 'nearby' relationship 
     * @param distance Integer distance in meters between the locations
     * 
     * @return true if the relationship was created; false if distance parameter is null,
     * database error occurred, or if a data model violation was detected.
     * 
     * @throws InvalidLocationId If either of the location id parameters are invalid
     * @throws LocationAlreadyNearbyLocation If a 'nearby' relationship already exists
     * between locations, or if the location ids are the same.
     */
    public boolean addNearby(String locIdA, String locIdB, Double distance) throws InvalidLocationId, LocationAlreadyNearbyLocation {
        LocationDao.instance.validOrException(locIdA, locIdB);        
        if (distance == null) {
            return false;
        }
        if (locIdA.equals(locIdB)) {
            throw new LocationAlreadyNearbyLocation();
        }
        try {
            if (getNearby(locIdA, locIdB) != null) {
                throw new LocationAlreadyNearbyLocation();
            }
        } catch (InvalidModel ex) {
            return false;
        }
        Vertex out;
        Vertex in;
        Edge e;
        try {
            out = ProxStorGraph.instance.getVertex(locIdA);
            in = ProxStorGraph.instance.getVertex(locIdB);
            e = ProxStorGraph.instance.addEdge(out, in, "nearby");
            e.setProperty("distance", distance);
            /* 
             * temporary implementation to speed up Blueprints VertexQuery
             * using Gremlin later will remove the need for this
             */
            e.setProperty("_target", locIdB);
            ProxStorGraph.instance.commit();
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(NearbyDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
        return true;
    }


    /*
     * retrieves Nearby relationship (Edge) between UserA and UserB
     *
     * returns Edge if found
     * returns null if not found
     * throws InvalidLocationId if either location id is invalid
     * throws InvalidModel if multiple nearby relationships found between locations
     */
    public Edge getNearby(String locIdA, String locIdB) throws InvalidLocationId, InvalidModel {
        LocationDao.instance.validOrException(locIdA, locIdB);
        try {
            // this is painful without Gremlin
            VertexQuery vq = ProxStorGraph.instance.getVertex(locIdA).query();
            vq.direction(BOTH);
            vq.labels("nearby");
            vq.has("_target", locIdB);
            long c = vq.count();
            if (c == 1) {
                return vq.edges().iterator().next();
            }
            if (c > 1) {
                throw new InvalidModel();
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(NearbyDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /*
     * updates a Nearby relationship between locations
     *
     * returns true if relationship is updated 
     */
    public boolean updateNearby(String locIdA, String locIdB, Double distance) throws InvalidLocationId {
        LocationDao.instance.validOrException(locIdA, locIdB);
        Edge e;
        try {
            e = getNearby(locIdA, locIdB);
        } catch (InvalidModel ex) {
            return false;
        }
        if (e != null) {
            e.setProperty("distance", distance);
            try {
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException ex) {
                Logger.getLogger(NearbyDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /*
     * removes an established Nearby relationship between locations
     *
     * returns true if succesful
     * returns false if a Nearby relationship was not already established
     * throws InvalidLocationID if either locID is invalid
     */
    public boolean removeNearby(String locIdA, String locIdB) throws InvalidLocationId {
        Edge e;
        try {
            e = getNearby(locIdA, locIdB);
            if (e != null) {
                e.remove();
                ProxStorGraph.instance.commit();                
                return true;
            }
        } catch (InvalidModel | ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(NearbyDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
     public double distanceBetweenLocations(Location locA, Location locB) {      
      /*
       * haversign
       * from http://www.codecodex.com/wiki/Calculate_distance_between_two_points_on_a_globe#Java
       */
      double R = 6372797.560856;
      double lat1 = locA.getLatitude();  
      double lat2 = locB.getLatitude();  
      double lon1 = locA.getLongitude();  
      double lon2 = locB.getLongitude();  
      double dLat = Math.toRadians(lat2-lat1);  
      double dLon = Math.toRadians(lon2-lon1);  
      double a = Math.sin(dLat/2) * Math.sin(dLat/2) +  
         Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *  
         Math.sin(dLon/2) * Math.sin(dLon/2);  
      double c = 2 * Math.asin(Math.sqrt(a));
      double distance = R * c;
      ProxStorDebug.println("distanceBetweenLocations " + locA.getDescription() + " & " + locB.getDescription() + " = " + distance);
      return distance;
    }

    /**      
     * create a "bounding box" centered around the (lat, lon) point
     *
     * @param gq
     * @param lat
     * @param lon
     * @param d
     * @return 
     */
    private GraphQuery queryDistanceBoundingBox(GraphQuery gq, double lat, double lon, double d) {
        double R = 6372797.560856;      
        
        d = Math.sqrt(2*(d*d));
        
        double bearing = Math.toRadians(315);
        double lat1 = Math.toDegrees((d/R) * Math.cos(bearing)) + lat;
        double lon1 = Math.toDegrees((d/(R * Math.sin(Math.toRadians(lat1)))) * Math.sin(bearing)) + lon;
        
        bearing = Math.toRadians(135);
        double lat2 = Math.toDegrees((d/R) * Math.cos(bearing)) + lat;
        double lon2 = Math.toDegrees((d/(R * Math.sin(Math.toRadians(lat2)))) * Math.sin(bearing)) + lon;

        ProxStorDebug.println("lat1, lon1 : " + lat1 + ", " + lon1);        
        ProxStorDebug.println("lat2, lon2 : " + lat2 + ", " + lon2);
        
        if (lat1 > lat2) {
            gq.has("latitude", LESS_THAN_EQUAL, lat1);
            gq.has("latitude", GREATER_THAN_EQUAL, lat2);
        } else {
            gq.has("latitude", GREATER_THAN_EQUAL, lat1);
            gq.has("latitude", LESS_THAN_EQUAL, lat2);
        }
        
        if (lon1 > lon2) {
            gq.has("longitude", LESS_THAN_EQUAL, lon1);
            gq.has("longitude", GREATER_THAN_EQUAL, lon2);
        } else {
            gq.has("longitude", GREATER_THAN_EQUAL, lon1);
            gq.has("longitude", LESS_THAN_EQUAL, lon2);
        }
        
        return gq;
    }  
     
}

