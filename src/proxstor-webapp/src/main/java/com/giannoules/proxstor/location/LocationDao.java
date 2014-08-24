package com.giannoules.proxstor.location;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
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
     * converts vertex into Location object
     *
     * assumes sanity check already performed on vertex
     */
    private Location vertexToLocation(Vertex v) {
        if (v == null) {
            return null;
        }
        Location l = new Location();
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
     * test Vertex for Location-ness
     */
    private boolean validLocationVertex(Vertex v) {
        return (v != null) && v.getProperty("_type").equals("location");
    }

    /*
     * test location id for Location-ness
     */
    private boolean validLocationId(String locId) {
        try {
            return (locId != null) && validLocationVertex(ProxStorGraph.instance.getVertex(locId));
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /*
     * abstract away setting of Vertex location type
     */
    private void setVertexToLocationType(Vertex v) {
        if (v != null) {
            v.setProperty("_type", "location");
        }
    }

    /*
     * returns Location stored under locId
     *
     * returns null if:
     *   - locId does not map to any graph vertex
     *   - vertex is not of type location
     *
     */
    public Location getLocationById(String locId) {
        if (locId == null) {
            return null;
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(locId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        if ((v != null) && validLocationVertex(v)) {
            return vertexToLocation(v);
        }
        return null;
    }

    /*
     * returns all locations in database with description desc
     */
    public List<Location> getLocationsByDescription(String desc) {
        List<Location> devices = new ArrayList<>();
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "location");
        q.has("description", desc);
        for (Vertex v : q.vertices()) {
            if (validLocationVertex(v)) {
                devices.add(vertexToLocation(v));
            }
        }
        return devices;
    }
    
    /*
     * find all matching Locations based on partially specified Location
     */
    public Collection<Location> getMatchingLocations(Location partial) {
        List<Location> locations = new ArrayList<>();
        if ((partial.getLocId() != null) && (!partial.getLocId().isEmpty())) {
            locations.add(getLocationById(partial.getLocId()));
            return locations;
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
            if (validLocationVertex(v)) {
                locations.add(vertexToLocation(v));
            }
        }
        return locations;
    }

    /*
     * returns *all* Locations in database     
     */
    public Collection<Location> getAllLocations() {
        List<Location> devices = new ArrayList<>();
        try {
            for (Vertex v : ProxStorGraph.instance.getVertices("_type", "location")) {
                devices.add(vertexToLocation(v));
            }
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return devices;
    }

    /* 
     * adds a new Location to the Graph database
     *
     * returns Location updated with actual locId used in the running
     * graph database instance
     */
    public Location addLocation(Location l) {
        if (l == null) {
            return null;
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.addVertex();
            v.setProperty("description", l.getDescription());
            setVertexToLocationType(v);
            ProxStorGraph.instance.commit();
            l.setLocId(v.getId().toString());

        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return l;
    }

    /*
     * updates Location based on Locations's locId
     *
     * returns true if valid locId
     * returns false is locId invalid
     */
    public boolean updateLocation(Location l) {
        if ((l == null) || (l.getLocId() == null)) {
            return false;
        }
        if (validLocationId(l.getLocId())) {
            Vertex v;
            try {
                v = ProxStorGraph.instance.getVertex(l.getLocId());
                v.setProperty("description", l.getDescription());
                ProxStorGraph.instance.commit();
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        }
        return false;
    }

    /* 
     * remove locId from graph
     *
     * returns true upon success
     * returns false if locId was not a Location
     */
    public boolean deleteLocation(String locId) {
        if ((locId != null) && (validLocationId(locId))) {
            try {
                ProxStorGraph.instance.getVertex(locId).remove();
                ProxStorGraph.instance.commit();
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        }
        return false;
    }

}
