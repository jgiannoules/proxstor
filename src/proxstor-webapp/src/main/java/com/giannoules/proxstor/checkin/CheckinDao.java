package com.giannoules.proxstor.checkin;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.device.DeviceDao;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocalityId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidEnvironmentalId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.exception.EnvironmentalNotContainedWithinLocation;
import com.giannoules.proxstor.exception.UserAlreadyInLocation;
import com.giannoules.proxstor.locality.LocalityDao;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.environmental.EnvironmentalDao;
import com.giannoules.proxstor.nearby.NearbyDao;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Compare.EQUAL;
import static com.tinkerpop.blueprints.Compare.GREATER_THAN_EQUAL;
import static com.tinkerpop.blueprints.Compare.LESS_THAN_EQUAL;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

/**
 * Checkin operations interface to ProxStor back-end
 * 
 * @author Jim Giannoules
 */

public enum CheckinDao {

    instance;
    
    /*
     * common methods used throughout CheckinDao
     */
    
    /**
     * Given a user return their current Locality (and thus Location), or null
     * when there is no current Locality.
     *
     * @param userId Object ID of the User
     * @return Locality if userId has current Locality; null otherwise
     * @throws com.giannoules.proxstor.exception.InvalidUserId if userId is not
     * a valid User object id.
     */
    public Locality getCurrentLocality(String userId) throws InvalidUserId {
        UserDao.instance.validOrException(userId);        
        try {
            Vertex v = getCurrentLocalityVertex(userId);
            if (v != null) {
                return LocalityDao.instance.get(v);
            }
        } catch (InvalidLocalityId ex) {
            Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
      
    /**
     * Lookup and retrieve the current Locality of userId
     *
     * @param userId The User userId to reference
     * @return Vertex of userId if active Locality, null otherwise
     * @throws com.giannoules.proxstor.exception.InvalidUserId if provided
     * userId is not valid
     */
    private Vertex getCurrentLocalityVertex(String userId) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        try {
            Vertex u = ProxStorGraph.instance.getVertex(userId);
            Iterable<Vertex> i = u.getVertices(OUT, "currently_at");
            Iterator<Vertex> it = i.iterator();
            if (it.hasNext()) {
                return it.next();
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
  
    public List<Locality> getPreviousLocalitiesDateRange(String userId, Date start, Date end, int max) throws InvalidUserId {
        UserDao.instance.validOrException(userId);        
        if ((start != null) && (end != null)) {
            List<Locality> localities = new ArrayList<>();
            try {
                VertexQuery vq = ProxStorGraph.instance.getVertex(userId).query();
                vq.direction(OUT);
                vq.labels("previously_at");
                vq.has("arrival", LESS_THAN_EQUAL, new DateTime(end).getMillis());
                vq.has("departure", GREATER_THAN_EQUAL, new DateTime(start).getMillis());
                vq.limit(max);                
                for (Vertex v : vq.vertices()) {                    
                    localities.add(LocalityDao.instance.get(v));                    
                }
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID | InvalidLocalityId ex) {
                Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return localities;
        }
        return null;        
    }
    
     public List<Locality> getPreviousLocalitiesDateRangeLocation(String userId, Date start, Date end, String locId, int max) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        if ((start != null) && (end != null)) {
            List<Locality> localities = new ArrayList<>();
            try {
                VertexQuery vq = ProxStorGraph.instance.getVertex(userId).query();
                vq.direction(OUT);
                vq.labels("previously_at");
                vq.has("arrival", LESS_THAN_EQUAL, new DateTime(end).getMillis());
                vq.has("departure", GREATER_THAN_EQUAL, new DateTime(start).getMillis());
                vq.has("locationId", EQUAL, locId);
                vq.limit(max);
                for (Vertex v : vq.vertices()) {
                    localities.add(LocalityDao.instance.get(v));
                }
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID | InvalidLocalityId ex) {
                Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return localities;
        }
        return null;        
     }
    
    public List<Locality> getPreviousLocalities(String userId) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        List<Vertex> localityVertices = getPreviousLocalityVertices(userId);
        List<Locality> localities = new ArrayList<>();
        try {            
            for (Vertex v : localityVertices) {
                localities.add(LocalityDao.instance.get(v));
            }
        } catch (InvalidLocalityId ex) {
            Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return localities;        
    }

    private List<Vertex> getPreviousLocalityVertices(String userId) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        List<Vertex> localities = new ArrayList<>();
        try {
            VertexQuery vq = ProxStorGraph.instance.getVertex(userId).query();
            vq.direction(OUT);
            vq.labels("previously_at");                      
            for (Vertex v : vq.vertices()) {
                localities.add(v);
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return localities;      
    }

    /**
     * Turn a currently_at Locality into a previously_at one
     * @param current
     * @return
     * @throws InvalidUserId 
     */
    private boolean userCurrentLocalityToPrevious(String userId) throws InvalidUserId {
        UserDao.instance.validOrException(userId);        
        Vertex current = getCurrentLocalityVertex(userId);
        if (current != null) {            
            // update Locality to inactive and the date now
            DateTime now = new DateTime();
            current.setProperty("active", false);
            current.setProperty("departure", now.toString());
            try {
                Vertex u = ProxStorGraph.instance.getVertex(userId);
                // remove all currently_at edges (Yes, there should never be >1)
                for (Edge e : u.getEdges(OUT, "currently_at")) {
                    e.remove();
                }                
                              
                /*
                 * add new previously_at edge with the following properties:
                 *   - date start
                 *   - date end                 
                 *   - location id
                 */
                Edge e = u.addEdge("previously_at", current);
                e.setProperty("arrival", new DateTime(current.getProperty("arrival")).getMillis());
                e.setProperty("departure", now.getMillis());                
                e.setProperty("locationId", current.getProperty("locationId"));
                
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /*
     * methods related to devices detecting environmentals
     */

    /**
     * When a Device detects a Environmental then ProxStor can infer that the User of
     * the device is the Location containing Environmental.
     *
     * @param devId
     * @param partial
     * @return
     * @throws InvalidEnvironmentalId
     * @throws InvalidDeviceId
     * @throws InvalidUserId
     * @throws InvalidLocationId
     * @throws EnvironmentalNotContainedWithinLocation
     */
    public Locality deviceDetectEnvironmental(String devId, Environmental partial) throws InvalidEnvironmentalId, InvalidDeviceId, InvalidUserId, InvalidLocationId, EnvironmentalNotContainedWithinLocation, UserAlreadyInLocation {
        Vertex user = DeviceDao.instance.getDeviceUserVertex(devId);
        Collection<Environmental> matches = EnvironmentalDao.instance.getMatching(partial);
        if ((matches == null) || (matches.size() != 1)) {
            return null;    // only one match allowed
        }
        String environmentalId = matches.iterator().next().getEnvironmentalId();        
        String locId = EnvironmentalDao.instance.getEnvironmentalLocation(environmentalId);
        String userId = user.getId().toString();        

        if (userInLocation(userId, locId)) {
            throw new UserAlreadyInLocation();
        }
        
        userCurrentLocalityToPrevious(userId);

        Locality l = new Locality();
        l.setActive(true);
        l.setArrival(new Date());
        l.setUserId(userId);
        l.setDeviceId(devId);
        l.setLocationId(locId);
        l.setEnvironmentalId(environmentalId);
        l.setManual(false);

        l = LocalityDao.instance.add(l);

        try {
            Vertex v = ProxStorGraph.instance.getVertex(l.getLocalityId());
            user.addEdge("currently_at", v);
            ProxStorGraph.instance.commit();
            return l;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * When a device no longer detects a environmental, then ProxStor can infer that
     * the Environmental's Location is no longer the User's current location. Care must
     * be taken that the User was indeed in the referenced location.
     *
     * @param devId
     * @param partial
     * @return
     * @throws com.giannoules.proxstor.exception.InvalidDeviceId
     * @throws com.giannoules.proxstor.exception.InvalidEnvironmentalId
     * @throws com.giannoules.proxstor.exception.InvalidUserId
     */
    public boolean deviceUndetectEnvironmental(String devId, Environmental partial) throws InvalidDeviceId, InvalidEnvironmentalId, InvalidUserId {
        Vertex user = DeviceDao.instance.getDeviceUserVertex(devId);
        Collection<Environmental> matches = EnvironmentalDao.instance.getMatching(partial);
        if ((matches == null) || (matches.size() != 1)) {
            return false;   // only one match allowed
        }
        String environmentalId = matches.iterator().next().getEnvironmentalId();
        String locId = EnvironmentalDao.instance.getEnvironmentalLocation(environmentalId);
        String userId = user.getId().toString();

        if (userInLocation(userId, locId)) {        
            return userCurrentLocalityToPrevious(userId);
        }
        return false;
    }
    
    /*
     * the following methods are used when a user manually specifies a location
     */
    
    private boolean userInLocation(String userId, String locId) throws InvalidUserId {
        Locality l = getCurrentLocality(userId);
        return (l != null) && (l.getLocationId().equals(locId));
    }

    /**
     *  Manually specify a user's current location. Any currently-at Locality
     *  will be moved into a previously-at relationship.
     * @param userId
     * @param locId
     * @return
     * @throws InvalidUserId
     * @throws InvalidLocationId 
     * @throws com.giannoules.proxstor.exception.UserAlreadyInLocation 
     */
    public Locality setUserLocation(String userId, String locId) throws InvalidUserId, InvalidLocationId, UserAlreadyInLocation {
        LocationDao.instance.validOrException(locId);
        Vertex user;
        try {
            if (userInLocation(userId, locId)) {
                throw new UserAlreadyInLocation();
            }

            user = ProxStorGraph.instance.getVertex(userId);
            userCurrentLocalityToPrevious(userId);

            Locality l = new Locality();
            l.setUserId(userId);
            l.setActive(true);
            l.setArrival(new Date());
            l.setLocationId(locId);
            l.setManual(true);

            l = LocalityDao.instance.add(l);

            user.addEdge("currently_at", ProxStorGraph.instance.getVertex(l.getLocalityId()));            
                        
            ProxStorGraph.instance.commit();
            return l;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID | InvalidDeviceId | InvalidEnvironmentalId | EnvironmentalNotContainedWithinLocation ex) {
            Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * 
     * @param userId
     * @param locId
     * @return
     * @throws InvalidUserId
     * @throws InvalidLocationId 
     */
    public boolean unsetUserLocation(String userId, String locId) throws InvalidUserId, InvalidLocationId {
        return (userInLocation(userId, locId)) 
                && (userCurrentLocalityToPrevious(userId));
    }

}
