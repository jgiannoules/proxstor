package com.giannoules.proxstor.locality;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.device.DeviceDao;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocalityId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidSensorId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.exception.SensorNotContainedWithinLocation;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.sensor.SensorDao;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
        List<Locality> localities = new ArrayList<>();
         if ((partial.getLocalityId() != null) && (!partial.getLocalityId().isEmpty())) {
            // invalid localityID is not an exception, it is just no match condition
            try { 
                localities.add(LocalityDao.this.get(partial.getLocalityId()));
                return localities;
            } catch (InvalidLocalityId ex) {
                Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "locality");
        /*
         * not including arrival and departure DateTime due to extreme unlikely
         * condition that a search query would match.
         * @TODO allow ranges to be specified (e.g. a Locality arrive between a and b)
         */
        if ((partial.getUserId() != null) && (!partial.getUserId().isEmpty())) {
            q.has("userId", partial.getUserId());
        }
        if ((partial.getDeviceId() != null) && (!partial.getDeviceId().isEmpty())) {
            q.has("deviceId", partial.getDeviceId());
        }
        if ((partial.getLocationId() != null) && (!partial.getLocationId().isEmpty())) {
            q.has("locationId", partial.getLocationId());
        }
        if ((partial.getSensorId() != null) && (!partial.getSensorId().isEmpty())) {
            q.has("sensorId", partial.getSensorId());
        }
        for (Vertex v : q.vertices()) {
            if (LocalityDao.this.valid(v)) {
                localities.add(toLocality(v));
            }
        }
        return localities;
    }

    public Locality add(Locality l) throws InvalidLocationId, InvalidDeviceId, 
            InvalidSensorId, SensorNotContainedWithinLocation {
        if (l == null) {
            return null;
        }

        l.setArrival(new Date());

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
                v.setProperty("locationId", l.getLocationId());
            }
            if (l.getUserId() != null) {
                v.setProperty("userId", l.getUserId());
            }
            if (l.getDeviceId() != null) {
                v.setProperty("deviceId", l.getDeviceId());
            }
            if (l.getSensorId() != null) {
                v.setProperty("sensorId", l.getSensorId());
            }
            v.setProperty("manual", l.isManual());
            v.setProperty("active", l.isActive());            
            if (l.getArrival() != null) {
                v.setProperty("arrival", (new DateTime(l.getArrival())).toString());
            }
            if (l.getDeparture() != null) {
                v.setProperty("departure", (new DateTime(l.getDeparture())).toString());
            }
            setType(v);            
            ProxStorGraph.instance.commit();
            l.setLocalityId(v.getId().toString());
            return l;
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
            try {
                ProxStorGraph.instance.rollback();
            } catch (ProxStorGraphDatabaseNotRunningException ex1) {
                Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return null;
        }
    }

    /*
     * updates Locality based on Locality's localityId
     *
     * returns true if valid localityId
     * throws InvalidLocalityId if id is invalid
     *     
     */
    public boolean update(Locality l) throws InvalidLocalityId {
        validOrException(l.getLocalityId());
        try {
            boolean updated = false;
            Vertex v = ProxStorGraph.instance.getVertex(l.getLocalityId());
            if (l.getArrival() != null) {
                v.setProperty("arrrival", (new DateTime(l.getArrival())).toString());
                updated = true;
            }
            if (l.getDeparture() != null) {
                v.setProperty("departure", (new DateTime(l.getDeparture())).toString());
                updated = true;
            }
            if (l.getUserId() != null) {
                v.setProperty("userId", l.getUserId());
                updated = true;
            }
            if (l.getDeviceId() != null) {
                v.setProperty("deviceId", l.getDeviceId());
                updated = true;
            }
            if (l.getLocationId() != null) {
                v.setProperty("locationId", l.getLocationId());
                updated = true;
            }
            if (l.getSensorId() != null) {
                v.setProperty("sensorId", l.getSensorId());
                updated = true;
            }
            if (updated) {
                ProxStorGraph.instance.commit();
                return true;
            }
        } catch (ProxStorGraphDatabaseNotRunningException| ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);            
        }        
        return false;
    }
    
     /* 
     * remove localityId from graph
     *
     * returns true upon success
     * throws InvalidLocalityId if localityId invalid
     *
     */
    public boolean delete(String localityId) throws InvalidLocalityId {
        validOrException(localityId);        
        try {
            ProxStorGraph.instance.getVertex(localityId).remove();
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocalityDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }        
    }

    public void validOrException(String ... ids) throws InvalidLocalityId {
        if (!valid(ids)) {
            throw new InvalidLocalityId();
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
    public Locality toLocality(Vertex v) {
        if (v == null) {
            return null;
        }
        Locality l = new Locality();
        /*
         * note: getProperty() will return null for non-existent props
         */
        l.setActive((Boolean) v.getProperty("active"));
        l.setManual((Boolean) v.getProperty("manual"));       
        if (v.getProperty("arrival") != null) {
            l.setArrival(new DateTime(v.getProperty("arrival")).toDate());
        }
        if (v.getProperty("departure") != null) {
            l.setDeparture(new DateTime(v.getProperty("departure")).toDate());
        }
        l.setUserId((String) v.getProperty("userId"));
        l.setDeviceId((String) v.getProperty("deviceId"));
        l.setLocationId((String) v.getProperty("locationId"));
        l.setSensorId((String) v.getProperty("sensorId"));
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
