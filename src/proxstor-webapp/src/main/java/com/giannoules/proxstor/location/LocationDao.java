package com.giannoules.proxstor.location;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Data Access Object to database-persistent store of Locations
 *
 * @TODO implement caching
 *
 * Currently uses basic low-level Blueprints API
 *
 */
public enum LocationDao {

    instance;

    private LocationDao() {
    }

   
    /*
     * allow one or more locId to be tested for validity
     *
     * return true iff all string params are valid locId, false otherwise
     *
     * no exceptions thrown. lack of database access silenty covered as false return
     *
     */
    public boolean valid(String... ids) {
        if (ids == null) {
            return false;
        }
        try {
            for (String id : ids) {
                if ((id == null) || !valid(ProxStorGraph.instance.getVertex(id))) {
                    return false;
                }
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    /*
     * test Vertex for Location-ness
     *
     * returns true if Vertex is of type Location, false otherwise
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
            if ((type == null) || (!type.equals("location"))) {
                return false;
            }
        }
        return true;
    }
    
     /**
     * Returns Location representation stored in back-end graph database under the 
     * specified Vertex object ID.
     *
     * @param locId    The location id (object id) to used to to retrieve Location
     * @return         Location representation of Vertex location id, or null if unable to access database
     * @throws InvalidLocationId    If the locId parameter is invalid
     */
    public Location get(String locId) throws InvalidLocationId {
        try {
            Vertex v;
            v = ProxStorGraph.instance.getVertex(locId);
            if (!valid(v)) {
                throw new InvalidLocationId();
            }
            return toLocation(v);
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (ProxStorGraphNonExistentObjectID ex) {
            throw new InvalidLocationId();
        }
    }

    /*
     * returns Location stored under Vertex v
     *
     * returns null if:
     *   - v is null     
     *   - vertex is not of type location
     *      
     * throws InvalidLocationId if parameter is not valid id
     */
    public Location get(Vertex v) throws InvalidLocationId {
        if (LocationDao.this.valid(v)) {
            return toLocation(v);
        }
        throw new InvalidLocationId();
    }
    
    /*
     * find all matching Locations based on partially specified Location
     *
     * returns all matching locations as a collection, or null if there are none
     * 
     * graph database not running becomes a null return
     *
     * used by SearchResource @POST
     */
    public Collection<Location> getMatching(Location partial) {
        List<Location> locations = new ArrayList<>();
        if ((partial.getLocId() != null) && (!partial.getLocId().isEmpty())) {
            // invalid locId is not an exception, it is just no match condition
            try {
                locations.add(LocationDao.this.get(partial.getLocId()));
                return locations;
            } catch (InvalidLocationId ex) {
                Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "location");
        if ((partial.getDescription() != null) && (!partial.getDescription().isEmpty())) {
            q.has("description", partial.getDescription());
        }
        for (Vertex v : q.vertices()) {
            if (LocationDao.this.valid(v)) {
                locations.add(toLocation(v));
            }
        }
        return locations;
    }

    /* 
     * adds a new Location to the Graph database
     *
     * returns Location updated with actual locID used in the running
     * graph database instance
     *
     * returns null if unable to add User
     *
     * used by LocationsResource @POST
     */
    public Location add(Location l) {
        if (l == null) {
            return null;
        }
        try {
            Vertex v = ProxStorGraph.instance.addVertex();
            v.setProperty("description", l.getDescription());
            setType(v);
            ProxStorGraph.instance.commit();
            l.setLocId(v.getId().toString());
            return l;
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /*
     * updates Location based on Locations's locId
     *
     * returns true if valid locId
     * throws InvalidLocationId if id is invalid
     *
     * used by LocationResource @PUT
     */
    public boolean update(Location l) throws InvalidLocationId {
       validOrException(l.getLocId());
        try {
            Vertex v = ProxStorGraph.instance.getVertex(l.getLocId());
            if (l.getDescription()!= null) {
                v.setProperty("description", l.getDescription());
            }          
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }        
    }

    /* 
     * remove locId from graph
     *
     * returns true upon success
     * throws InvalidLocationId if locId invalid
     *
     * used by LocationResource @DELETE
     */
    public boolean delete(String locId) throws InvalidLocationId {
        validOrException(locId);
        try {
            ProxStorGraph.instance.getVertex(locId).remove();
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void validOrException(String ... ids) throws InvalidLocationId {
        if (!valid(ids)) {
            throw new InvalidLocationId();
        }
    }
    
    /*
     * 2**64-1 = 18,446,744,073,709,551,615
     * 18,446,744,073,709,551,615 meters = 1,949.822 light years
     */
    public boolean setNearbyLocation(String locIdA, String locIdB, Long distance) {
        try {
            // @TODO
            // check if already nearby. let exception throwing for invalid ID go back to caller
            // at this point the IDs must be valid and must be locations
            ProxStorGraph.instance.addEdge(
                    ProxStorGraph.instance.getVertex(locIdA),
                    ProxStorGraph.instance.getVertex(locIdB),
                    "nearby").setProperty("distance", distance);
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    
    // ----> BEGIN private methods <----
    
    
    /*
     * converts vertex into Location object
     *
     * (assumes sanity check already performed on vertex)
     *
     * returns non-null Location if successful, otherwise null
     *
     * no exceptions thrown
     */
    private Location toLocation(Vertex v) {
        if (v == null) {
            return null;
        }
        Location l = new Location();
        /*
         * note: getProperty() will return null for non-existent props
         */ 
        l.setDescription((String) v.getProperty("description"));
        Object id = v.getId();
        if (id instanceof Long) {
            l.setLocId(Long.toString((Long) v.getId()));
        } else {
            l.setLocId(v.getId().toString());
        }
        return l;
    }

    /*
     * abstract away setting of Vertex Location type to allow underlying graph
     * representation/management to evolve
     */
    private void setType(Vertex v) {
        if (v != null) {
            v.setProperty("_type", "location");
        }
    }
    
}
