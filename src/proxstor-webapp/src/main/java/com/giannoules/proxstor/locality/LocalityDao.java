package com.giannoules.proxstor.locality;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.api.SensorType;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.device.DeviceDao;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocalityId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidSensorId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.exception.SensorNotContainedWithinLocation;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

/*
 * Data Access Object to database-persistent store of Localities
 *
 * @TODO implement caching
 *
 * Currently uses only basic low-level Blueprints API
 *
 */
public enum LocalityDao {

    instance;

    private LocalityDao() {
    }

    /*
     * allow one or more localityIds to be tested for validity
     *
     * return true iff all string params are valid localityId, false otherwise
     *
     * no exceptions thrown. exceptions silenty covered as false return.
     */
    public boolean valid(String... localityIds) {
        if (localityIds == null) {
            return false;
        }
        try {
            for (String id : localityIds) {
                if (!LocalityDao.this.valid(ProxStorGraph.instance.getVertex(id))) {
                    return false;
                }
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /*
     * test Vertex for Locality-ness
     *
     * returns true if Vertex is of type Locality, false otherwise
     * 
     * no exceptions thrown
     */
    public boolean valid(Vertex... vertices) {
        if (vertices == null) {
            return false;
        }
        String type;
        for (Vertex v : vertices) {
            type = v.getProperty("_type");
            if ((type == null) || (!type.equals("locality"))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns Locality representation stored in back-end graph database under
     * the specified Vertex object ID.
     * <p>
     * Used by LocalityResource @GET and numerous other places
     *
     * @param localityId The locality id (object id) to used to to retrieve
     * Locality
     * @return Locality representation of Vertex locality id, or null if unable
     * to access database
     * @throws InvalidLocalityId If the localityId parameter is invalid
     */
    public Locality get(String localityId) throws InvalidLocalityId {
        try {
            Vertex v;
            v = ProxStorGraph.instance.getVertex(localityId);
            if (!valid(v)) {
                throw new InvalidLocalityId();
            }
            return toLocality(v);
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (ProxStorGraphNonExistentObjectID ex) {
            throw new InvalidLocalityId();
        }
    }

    
    /**
     * Lookup and retrieve the current Locality of userId
     * 
     * @param userId The User userId to reference
     * @return Locality if userId has active Locality, null otherwise
     * @throws com.giannoules.proxstor.exception.InvalidUserId if provided userId is not valid
     */
    public Locality getCurrentLocality(String userId) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        try {
            Vertex u = ProxStorGraph.instance.getVertex(userId);
            Iterable<Vertex> i = u.getVertices(OUT, "currently_at");
            List<Vertex> localities = new ArrayList<>();
            for (Vertex v : localities) {            
                localities.add(v);
            }
            if (localities.size() == 1) {
                return toLocality(localities.get(0));
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return null;
    }
    
     /**
     * Lookup and retrieve the current Locality of userId
     * 
     * @param userId The User userId to reference
     * @return Vertex of userId if active Locality, null otherwise
     * @throws com.giannoules.proxstor.exception.InvalidUserId if provided userId is not valid
     */
    private Vertex getCurrentLocalityVertex(String userId) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        try {
            Vertex u = ProxStorGraph.instance.getVertex(userId);
            Iterable<Vertex> i = u.getVertices(OUT, "currently_at");
            List<Vertex> localities = new ArrayList<>();
            for (Vertex v : localities) {            
                localities.add(v);
            }
            if (localities.size() == 1) {
                return localities.get(0);
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return null;
    }
    
    
    /**
     * Returns the Vertex "previously_at" a given Vertex
     * 
     * @param v
     * @return 
     */
    private Vertex getPrevioulsyAt(Vertex v) {
        Iterable<Vertex> i = v.getVertices(OUT, "previously_at");
        List<Vertex> localities = new ArrayList<>();
        for (Vertex prevVertex : localities) {
            localities.add(prevVertex);
        }
        if (localities.size() == 1) {
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
                    localities.add(toLocality(w));
                    v = w;
                }
            } while (((count < depth) || (depth == 0)) && (w != null));
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return localities;
    }
    
    public boolean userCurrentLocalityToPrevious(String userId) throws InvalidUserId {
        Vertex current = getCurrentLocalityVertex(userId);
        if (current != null) {
            /*
             * Locality is now inactive
             */
            current.setProperty("active", false);
            try {
                /*
                 * reference to User Vertex
                 */
                Vertex u = ProxStorGraph.instance.getVertex(userId);
                /*
                * delete "currently_at" Edge
                */
                for (Edge e : u.getEdges(OUT, "currently_at")) {
                    e.remove();
                }                
                /*
                 * get the most recent previously_at vertex
                 */
                Vertex previous = getPreviousLocalityVertices(userId, 1).get(0);
                /*
                 * point the about-to-be-previously_at vertex to the previous one
                 */
                if (previous != null) {
                    current.addEdge("previously_at", previous);
                }
                /*
                 * delete "previously_at" Edge
                 */
                for (Edge e : u.getEdges(OUT, "previously_at")) {
                    e.remove();
                }     
                /*
                 * establish previoulsy_at from user to current
                 */
                u.addEdge("previously_at", current);
                
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
            }                        
        }
        return false;
    }
        
    /**
     * When a Device detects a Sensor then ProxStor can infer that the User of
     * the device is the Location containing Sensor.
     * 
     * @param devId
     * @param sensorId
     * @return
     * @throws InvalidSensorId
     * @throws InvalidDeviceId
     * @throws InvalidUserId
     * @throws InvalidLocationId
     * @throws SensorNotContainedWithinLocation 
     */
     
    public Locality deviceDetectSensor(String devId, String sensorId) throws InvalidSensorId, InvalidDeviceId, InvalidUserId, InvalidLocationId, SensorNotContainedWithinLocation {        
        Vertex user = DeviceDao.instance.getDeviceUserVertex(devId);       
        Vertex location = SensorDao.instance.getSensorLocationVertex(sensorId);
        String userId = user.getId().toString();
        String locId = location.getId().toString();

        userCurrentLocalityToPrevious(userId);
        
        Locality l = new Locality();
        l.setActive(true);
        l.setArrival(new DateTime());
        l.setDeviceId(devId);
        l.setLocationId(locId);
        l.setSensorId(sensorId);
        l.setManual(false);
        
        l = add(l);
        
        try {
            user.addEdge("currently_at", ProxStorGraph.instance.getVertex(l.getLocalityId()));
            ProxStorGraph.instance.commit();
            return l;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * When a device no longer detects a sensor, then ProxStor can
     * infer that the Sensor's Location is no longer the User's current location.
     * Care must be taken that the User was indeed in the referenced location.
     * 
     * @param devId
     * @param sensorId
     * @return 
     * @throws com.giannoules.proxstor.exception.InvalidDeviceId 
     * @throws com.giannoules.proxstor.exception.InvalidSensorId 
     * @throws com.giannoules.proxstor.exception.InvalidUserId 
     */
    public boolean deviceUndetectSensor(String devId, String sensorId) throws InvalidDeviceId, InvalidSensorId, InvalidUserId {
        Vertex user = DeviceDao.instance.getDeviceUserVertex(devId);       
        Vertex location = SensorDao.instance.getSensorLocationVertex(sensorId);
        String userId = user.getId().toString();
        String locId = location.getId().toString();
        
        Locality currentLocality = getCurrentLocality(userId);
        if ((currentLocality != null) && (currentLocality.getLocationId().equals(locId))) {
            userCurrentLocalityToPrevious(userId);
            return true;
        }           
        return false;
    }
    
    public Locality userInLocation(String userId, String locId) throws InvalidUserId, InvalidLocationId {
        UserDao.instance.validOrException(userId);
        LocationDao.instance.validOrException(locId);
        Vertex user;
        try {
            user = ProxStorGraph.instance.getVertex(userId);
            userCurrentLocalityToPrevious(userId);

            Locality l = new Locality();
            l.setActive(true);
            l.setArrival(new DateTime());
            l.setLocationId(locId);
            l.setManual(true);

            l = add(l);

            user.addEdge("currently_at", ProxStorGraph.instance.getVertex(l.getLocalityId()));
            ProxStorGraph.instance.commit();
            return l;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID | InvalidDeviceId | InvalidSensorId | SensorNotContainedWithinLocation ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /*
     * returns Locality stored under Vertex v
     *
     * returns null if:
     *   - v is null     
     *   - vertex is not of type locality
     *      
     * throws InvalidLocalityId if parameter is not valid id
     */
    public Locality get(Vertex v) throws InvalidLocalityId {
        if (LocalityDao.this.valid(v)) {
            return toLocality(v);
        }
        throw new InvalidLocalityId();
    }

    // @TODO
    public Collection<Locality> getMatching(Locality partial) {
        return null;
    }

    public Locality add(Locality l) throws InvalidLocationId, InvalidDeviceId, InvalidSensorId, SensorNotContainedWithinLocation {
        if (l == null) {
            return null;
        }
        System.out.println(l);

        l.setArrival(new DateTime());

        if (l.isManual()) {
            /*
             * ensure new Locality references back to a Location
             */
            LocationDao.instance.validOrException(l.getLocationId());
        } else {
            /*
             * if !manual we need device + sensor
             */
            DeviceDao.instance.validOrException(l.getDeviceId());
            String locId = SensorDao.instance.getSensorLocation(l.getSensorId());
            l.setLocationId(locId);
        }

        try {
            Vertex v = ProxStorGraph.instance.addVertex();
            /*
             * set properties of the Locality vertex            
             */
            if (l.getLocationId() != null) {
                v.setProperty("locId", l.getLocationId());
            }
            if (l.getDeviceId() != null) {
                v.setProperty("devId", l.getDeviceId());
            }
            if (l.getSensorId() != null) {
                v.setProperty("sensorId", l.getSensorId());
            }
            v.setProperty("manual", l.isManual());
            v.setProperty("active", l.isActive());            
            v.setProperty("arrive", l.getArrival().toString());            
            setType(v);
            /*
             * associate with User            
             */
//            Vertex user = ProxStorGraph.instance.getVertex(l.getDeviceId()).getVertices(IN, "uses").iterator().next();
//            user.addEdge("currently_at", v);
            ProxStorGraph.instance.commit();
            l.setLocalityId(v.getId().toString());
            return l;
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            try {
                ProxStorGraph.instance.rollback();
            } catch (ProxStorGraphDatabaseNotRunningException ex1) {
                Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return null;
        }
    }

    // ----> BEGIN private methods <----
    
    
    /*
     * converts vertex into Locality object
     *
     * (assumes sanity check already performed on vertex)
     *
     * returns non-null Locality if successful, otherwise null
     *
     * no exceptions thrown
     */
    private Locality toLocality(Vertex v) {
        if (v == null) {
            return null;
        }
        Locality l = new Locality();
        /*
         * note: getProperty() will return null for non-existent props
         */
        l.setActive((Boolean) v.getProperty("active"));
        l.setManual((Boolean) v.getProperty("manual"));
        l.setArrival(new DateTime(v.getProperty("arrive")));
        if (v.getProperty("departure") != null) {
            l.setDeparture(new DateTime(v.getProperty("departure")));
        }
        l.setDeviceId((String) v.getProperty("devId"));
        l.setLocationId((String) v.getProperty("locId"));
        l.setSensorId((String) v.getProperty("sensorId"));
//        l.setSensorType(SensorType.valueOf((String) v.getProperty("sensorType")));
//        l.setSensorValue((String) v.getProperty("sensorValue"));        
        //l.setSensors(v.getProperty("sensors"));
        Object id = v.getId();
        if (id instanceof Long) {
            l.setLocalityId(Long.toString((Long) v.getId()));
        } else {
            l.setLocalityId(v.getId().toString());
        }
        return l;
    }

    /*
     * abstract away setting of Vertex Locality type to allow underlying graph
     * representation/management to evolve
     */
    private void setType(Vertex v) {
        if (v != null) {
            v.setProperty("_type", "locality");
        }
    }

}
