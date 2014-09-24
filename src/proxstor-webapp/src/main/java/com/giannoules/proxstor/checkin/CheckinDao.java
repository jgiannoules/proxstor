package com.giannoules.proxstor.checkin;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.device.DeviceDao;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocalityId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidSensorId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.exception.SensorNotContainedWithinLocation;
import com.giannoules.proxstor.exception.UserAlreadyInLocation;
import com.giannoules.proxstor.exception.UserCurrentlyInMultipleLocalities;
import com.giannoules.proxstor.locality.LocalityDao;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.sensor.SensorDao;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

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
            Vertex u = ProxStorGraph.instance.getVertex(userId);           
            Iterable<Vertex> iter = u.getVertices(OUT, "currently_at");
            List<Vertex> localities = new ArrayList<>();
            for (Vertex v : iter) {
                localities.add(v);
            }
            // @TODO detect this condition?
//            if (localities.size() > 1) {
//                throw new UserCurrentlyInMultipleLocalities();
//            }
            if (localities.size() >= 1) {
                return LocalityDao.instance.get(localities.get(0));
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID | InvalidLocalityId ex) {
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
            Iterable<Vertex> iter = u.getVertices(OUT, "currently_at");
            List<Vertex> localities = new ArrayList<>();
            for (Vertex v : iter) {
                localities.add(v);
            }
            if (localities.size() >= 1) {
                return localities.get(0);
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Returns the Vertex "previously_at" a given Vertex (i.e. a User)
     *
     * @param v
     * @return
     */
    private Vertex getPrevioulsyAt(Vertex v) {
        Iterable<Vertex> i = v.getVertices(OUT, "previously_at");
        List<Vertex> localities = new ArrayList<>();
        for (Vertex prevVertex : i) {
            localities.add(prevVertex);
        }
        if (localities.size() >= 1) {
            return localities.get(0);
        }
        return null;
    }

    public List<Locality> getPreviousLocalities(String userId, int depth) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        int count = 0;
        List<Locality> localities = new ArrayList<>();
        Vertex v, w;
        try {
            v = ProxStorGraph.instance.getVertex(userId);
            do {
                w = getPrevioulsyAt(v);
                if (w != null) {
                    localities.add(LocalityDao.instance.toLocality(w));
                    v = w;
                }
            } while (((count < depth) || (depth == 0)) && (w != null));
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return localities;
    }

    private List<Vertex> getPreviousLocalityVertices(String userId, int depth) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        int count = 0;
        List<Vertex> localities = new ArrayList<>();
        Vertex v, w;
        try {
            v = ProxStorGraph.instance.getVertex(userId);
            do {
                w = getPrevioulsyAt(v);
                if (w != null) {
                    localities.add(w);
                    v = w;
                }
            } while (((count < depth) || (depth == 0)) && (w != null));
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return localities;
    }

    /**
     * Move a currently_at Locality into the head of the previously_at list
     * @param current
     * @return
     * @throws InvalidUserId 
     */
    private boolean userCurrentLocalityToPrevious(String userId) throws InvalidUserId {
        UserDao.instance.validOrException(userId);        
        Vertex current = getCurrentLocalityVertex(userId);
        if (current != null) {            
            current.setProperty("active", false);
            current.setProperty("departure", (new DateTime().toString()));
            try {
                Vertex u = ProxStorGraph.instance.getVertex(userId);                
                for (Edge e : u.getEdges(OUT, "currently_at")) {
                    e.remove();
                }                
                Vertex previous = getPrevioulsyAt(u);
                if (previous != null) {
                    current.addEdge("previously_at", previous);
                }                
                for (Edge e : u.getEdges(OUT, "previously_at")) {
                    e.remove();
                }                                     
                u.addEdge("previously_at", current);                
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(CheckinDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /*
     * methods related to devices detecting sensors
     */

    /**
     * When a Device detects a Sensor then ProxStor can infer that the User of
     * the device is the Location containing Sensor.
     *
     * @param devId
     * @param partial
     * @return
     * @throws InvalidSensorId
     * @throws InvalidDeviceId
     * @throws InvalidUserId
     * @throws InvalidLocationId
     * @throws SensorNotContainedWithinLocation
     */
    public Locality deviceDetectSensor(String devId, Sensor partial) throws InvalidSensorId, InvalidDeviceId, InvalidUserId, InvalidLocationId, SensorNotContainedWithinLocation, UserAlreadyInLocation {
        Vertex user = DeviceDao.instance.getDeviceUserVertex(devId);
        Collection<Sensor> matches = SensorDao.instance.getMatching(partial);
        if ((matches == null) || (matches.size() != 1)) {
            return null;    // only one match allowed
        }
        String sensorId = matches.iterator().next().getSensorId();        
        String locId = SensorDao.instance.getSensorLocation(sensorId);
        String userId = user.getId().toString();        

        if (userInLocation(userId, locId)) {
            throw new UserAlreadyInLocation();
        }
        
        userCurrentLocalityToPrevious(userId);

        Locality l = new Locality();
        l.setActive(true);
        l.setArrival(new Date());
        l.setDeviceId(devId);
        l.setLocationId(locId);
        l.setSensorId(sensorId);
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
     * When a device no longer detects a sensor, then ProxStor can infer that
     * the Sensor's Location is no longer the User's current location. Care must
     * be taken that the User was indeed in the referenced location.
     *
     * @param devId
     * @param partial
     * @return
     * @throws com.giannoules.proxstor.exception.InvalidDeviceId
     * @throws com.giannoules.proxstor.exception.InvalidSensorId
     * @throws com.giannoules.proxstor.exception.InvalidUserId
     */
    public boolean deviceUndetectSensor(String devId, Sensor partial) throws InvalidDeviceId, InvalidSensorId, InvalidUserId {
        Vertex user = DeviceDao.instance.getDeviceUserVertex(devId);
        Collection<Sensor> matches = SensorDao.instance.getMatching(partial);
        if ((matches == null) || (matches.size() != 1)) {
            return false;   // only one match allowed
        }
        String sensorId = matches.iterator().next().getSensorId();
        String locId = SensorDao.instance.getSensorLocation(sensorId);
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
            l.setActive(true);
            l.setArrival(new Date());
            l.setLocationId(locId);
            l.setManual(true);

            l = LocalityDao.instance.add(l);

            user.addEdge("currently_at", ProxStorGraph.instance.getVertex(l.getLocalityId()));
            ProxStorGraph.instance.commit();
            return l;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID | InvalidDeviceId | InvalidSensorId | SensorNotContainedWithinLocation ex) {
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
