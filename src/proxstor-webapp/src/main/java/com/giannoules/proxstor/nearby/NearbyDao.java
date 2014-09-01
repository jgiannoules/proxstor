package com.giannoules.proxstor.nearby;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidModel;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.LocationAlreadyNearbyLocation;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.exception.UserAlreadyKnowsUser;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Compare.GREATER_THAN_EQUAL;
import static com.tinkerpop.blueprints.Compare.LESS_THAN_EQUAL;
import com.tinkerpop.blueprints.Direction;
import static com.tinkerpop.blueprints.Direction.BOTH;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
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
    public Collection<Location> getLocationsNearby(String locId, Integer distanceVal, int limit) throws InvalidLocationId {
        LocationDao.instance.validOrException(locId);
        if (distanceVal != null) {
            List<Location> nearby = new ArrayList<>();
            try {
                VertexQuery vq = ProxStorGraph.instance.getVertex(locId).query();
                vq.direction(BOTH);
                vq.labels("nearby");
                vq.has("distance", LESS_THAN_EQUAL, distanceVal);
                vq.limit(limit);
                for (Vertex v : vq.vertices()) {
                    nearby.add(LocationDao.instance.get(v));
                }
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
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
    public boolean addNearby(String locIdA, String locIdB, Integer distance) throws InvalidLocationId, LocationAlreadyNearbyLocation {
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
    public boolean updateNearby(String locIdA, String locIdB, Integer distance) throws InvalidLocationId {
        LocationDao.instance.validOrException(locIdA, locIdB);
        Edge e;
        try {
            e = getNearby(locIdA, locIdB);
        } catch (InvalidModel ex) {
            return false;
        }
        if (e != null) {
            e.setProperty("distance", distance);
            return true;
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
                return true;
            }
        } catch (InvalidModel ex) {
            Logger.getLogger(NearbyDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}

