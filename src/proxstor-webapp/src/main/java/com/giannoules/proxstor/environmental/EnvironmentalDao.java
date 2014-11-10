package com.giannoules.proxstor.environmental;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.api.EnvironmentalType;
import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidParameter;
import com.giannoules.proxstor.exception.InvalidEnvironmentalId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.exception.EnvironmentalNotContainedWithinLocation;
import com.giannoules.proxstor.location.LocationDao;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum EnvironmentalDao {

    instance;

    private EnvironmentalDao() {
    }

    /**
     * Get a specific environmentalId contained in a particular lcdId. The environmental id
     * must exists as a valid environmental, the locid must exists as a valid location,
     * and the 'within' relationship must exists from location to environmental.
     *
     * @param locId String representation of location id
     * @param environmentalId String representation of environmental id
     *
     * @return Instance of matching Environmental
     *
     * @throws InvalidEnvironmentalId if the environmentalId is invalid
     * @throws InvalidLocationId if the locId is invalid
     * @throws EnvironmentalNotContainedWithinLocation if the environmental isn't within the
     * location
     */
    public Environmental getLocationEnvironmental(String locId, String environmentalId) 
            throws EnvironmentalNotContainedWithinLocation, InvalidEnvironmentalId, InvalidLocationId {
        ProxStorDebug.println("getLocationEnvironmental(" + locId + ", " + environmentalId + ")");
        LocationDao.instance.validOrException(locId);
        validOrException(environmentalId);
        if (isLocationEnvironmental(locId, environmentalId)) {
            return get(environmentalId);
        } else {
            throw new EnvironmentalNotContainedWithinLocation();
        }
    }

    /**
     * Returns all the environmentals within the location
     *
     * @param locId String representation of location id
     *
     * @return Collection of Environmental objects contained in location, or null if none
     *
     * @throws InvalidLocationId If the locId is invalid
     */
    public Collection<Environmental> getAllLocationEnvironmentals(String locId) throws InvalidLocationId {
        LocationDao.instance.validOrException(locId);
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(locId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        List<Environmental> environmentals = new ArrayList<>();
        for (Edge e : v.getEdges(OUT, "contains")) {
            environmentals.add(EnvironmentalDao.instance.toEnvironmental(e.getVertex(IN)));
        }
        return environmentals;
    }

    /**
     * Find and returns all matching environmentals based on partially specified Environmental
     * <p>
     * If the partially specified environmental includes a environmentalId field then that one
     * property is used to match. If the environmentalId is not present, then all other
     * properties are used using a GraphQuery to match. This means passing in a
     * environmentalId will allow you to retrieve a specific environmental and bypass the
     * normal requirement that environmental retrieval requires knowledge of the
     * location containing it.
     * <p>
     * Note that no exceptions are thrown. An invalid environmentalId is handled a
     * simply a null response.
     *
     * @param partial Partially completed Environmental object
     *
     * @return Collection of Environmental objects matching partial, or null if none
     */
    public Collection<Environmental> getMatching(Environmental partial) {
        List<Environmental> environmentals = new ArrayList<>();
        if ((partial.getEnvironmentalId() != null) && (!partial.getEnvironmentalId().isEmpty())) {
            try {
                validOrException(partial.getEnvironmentalId());
                environmentals.add(get(partial.getEnvironmentalId()));
                return environmentals;
            } catch (InvalidEnvironmentalId ex) {
                // invalid environmentalId is not an exception, it is just no match condition
                Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "environmental");
        if ((partial.getDescription() != null) && (!partial.getDescription().isEmpty())) {
            q.has("description", partial.getDescription());
        }
        if (partial.getType() != null) {
            q.has("type", partial.getType().toString());
        }
        if (partial.getIdentifier()!= null) {
            q.has("typeIdentifier", partial.getIdentifier());
        }
        for (Vertex v : q.vertices()) {
            if (valid(v)) {
                environmentals.add(toEnvironmental(v));
            }
        }
        return environmentals;
    }

    /**
     * Insert new Environmental instance into Graph associated with Location locId
     *
     * @param locId String representation of the Location containing the Environmental
     * @param e Environmental to be added. Note that the environmentalId will be ignored
     *
     * @return Environmental object with correct environmentalId reflecting object ID assigned
 by underlying graph;
     *
     * @throws InvalidLocationId If the locId parameter does not match a valid
     * location
     */
    public Environmental add(String locId, Environmental e) throws InvalidLocationId, InvalidParameter {
        LocationDao.instance.validOrException(locId);
        if ((e.getDescription() == null) || (e.getType() == null)) {
            throw new InvalidParameter();
        }
        try {
            Vertex out = ProxStorGraph.instance.getVertex(locId);
            Vertex in = ProxStorGraph.instance.addVertex();
            in.setProperty("description", e.getDescription());
            in.setProperty("type", e.getType().toString());
            in.setProperty("typeIdentifier", e.getIdentifier());
            setType(in);
            ProxStorGraph.instance.addEdge(out, in, "contains");
            ProxStorGraph.instance.commit();
            e.setEnvironmentalId(in.getId().toString());
            return e;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Updates (modifies) an existing environmental in the database. The environmental must be
     * in a 'Within' relationship with the Location. All fields from the Environmental
     * parameter will overwrite the fields of the original Environmental. The environmental id
     * will remain the same.
     *
     * @param locId String representation of the Location containing the environmental
     * @param e Updated Environmental object
     *
     * @return true if the update was successful; false if a database error was
     * encountered
     *
     * @throws InvalidLocationId If the locId parameter does not represent a
     * valid location
     * @throws InvalidEnvironmentalId If the dnvironmentalId within the s parameter does not
     * represent a valid environmental
     * @throws EnvironmentalNotContainedWithinLocation If the d.environmentalId and locId are
     * valid, but the environmental is not within the location.
     */
    public boolean update(String locId, Environmental e) throws EnvironmentalNotContainedWithinLocation, InvalidEnvironmentalId, InvalidLocationId {
        LocationDao.instance.validOrException(locId);
        validOrException(e.getEnvironmentalId());
        if (!isLocationEnvironmental(locId, e.getEnvironmentalId())) {
            throw new EnvironmentalNotContainedWithinLocation();
        }        
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(e.getEnvironmentalId());
            if (e.getDescription() != null) {
                v.setProperty("description", e.getDescription());
            }
            if (e.getType() != null) {
                v.setProperty("type", e.getType().toString());
            }
            if (e.getIdentifier()!= null) {
                v.setProperty("typeIdentifier", e.getIdentifier());
            }
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Remove a Environmental from the database. The location must have a 'within'
     * relationship to the environmental for the operation to succeed.
     *
     * @param locId String representation of the location id containing the
     * environmental
     * @param environmentalId String representation of the environmental to be removed
     *
     * @return true if the operation succeeds; false if communication error to
     * the database
     *
     * @throws InvalidEnvironmentalId If environmentalId parameter is not a valid environmental in
     * the graph
     * @throws InvalidLocationId If locId parameter is not a valid location in
     * the graph
     * @throws EnvironmentalNotContainedWithinLocation If the locId and environmentalId are
     * valid, but the environmental is not within the location.
     */
    public boolean delete(String locId, String environmentalId) throws InvalidEnvironmentalId, InvalidLocationId, EnvironmentalNotContainedWithinLocation {
        LocationDao.instance.validOrException(locId);
        validOrException(environmentalId);
        if (!isLocationEnvironmental(locId, environmentalId)) {
            throw new EnvironmentalNotContainedWithinLocation();
        }
        return delete(environmentalId);            
    }

    /**
     * Helper method which accepts a location id strings and either returns
     * nothing to the caller or throws an InvalidEnvironmentalId exception if any of
     * the environmental id are invalid.
     *
     * @param environmentalIds variadic list of environmental id String representations
     *
     * @throws InvalidEnvironmentalId If any of the environmentalId String parameters are not
     * valid environmentals
     */
    public void validOrException(String... environmentalIds) throws InvalidEnvironmentalId {
        if (!valid(environmentalIds)) {
            throw new InvalidEnvironmentalId();
        }
    }
            
    public Location getEnvironmentalLocation(Environmental e) {
        VertexQuery vq;
        try {
            vq = ProxStorGraph.instance.getVertex(e.getEnvironmentalId()).query();
            vq.direction(IN);
            vq.labels("contains");
            List<String> locations = new ArrayList<>();
            for (Vertex vertex : vq.vertices()) {
                locations.add(vertex.getId().toString());
            }
            if (locations.size() != 1) {
                return null;
            }
            return LocationDao.instance.get(locations.get(0));
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID | InvalidLocationId ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getEnvironmentalLocation(String environmentalId) throws InvalidEnvironmentalId {
        EnvironmentalDao.instance.validOrException(environmentalId);
        VertexQuery vq;
        try {
            vq = ProxStorGraph.instance.getVertex(environmentalId).query();
            vq.direction(IN);
            vq.labels("contains");
            List<String> locations = new ArrayList<>();
            for (Vertex vertex : vq.vertices()) {
                locations.add(vertex.getId().toString());
            }
            if (locations.size() != 1) {
                return null;
            }
            return locations.get(0);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
     public Vertex getEnvironmentalLocationVertex(String environmentalId) throws InvalidEnvironmentalId {
        EnvironmentalDao.instance.validOrException(environmentalId);
        VertexQuery vq;
        try {
            vq = ProxStorGraph.instance.getVertex(environmentalId).query();
            vq.direction(IN);
            vq.labels("contains");
            List<Vertex> locations = new ArrayList<>();
            for (Vertex vertex : vq.vertices()) {
                locations.add(vertex);
            }
            if (locations.size() != 1) {
                return null;
            }
            return locations.get(0);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    // ----> BEGIN private methods <----
    
    
    /*
     * converts vertex into Environmental object
     *
     * assumes sanity check already performed on vertex
     */
    private Environmental toEnvironmental(Vertex v) {
        if (v == null) {
            return null;
        }
        Environmental s = new Environmental();
        s.setDescription((String) v.getProperty("description"));
        s.setType(EnvironmentalType.valueOf((String) v.getProperty("type")));
        s.setIdentifier((String) v.getProperty("typeIdentifier"));
        Object id = v.getId();
        if (id instanceof Long) {
            s.setEnvironmentalId(Long.toString((Long) v.getId()));
        } else {
            s.setEnvironmentalId(v.getId().toString());
        }
        return s;
    }

    /*
     * abstract away setting of Vertex Environmental type
     */
    private void setType(Vertex v) {
        if (v != null) {
            v.setProperty("_type", "environmental");
        }
    }

    /* 
     * remove environmentalId from graph
     *
     * returns true upon success
     * returns false if environmentalId was not a Environmental
     */
    private boolean delete(String environmentalId) {
        if ((environmentalId != null) && (valid(environmentalId))) {
            try {
                ProxStorGraph.instance.getVertex(environmentalId).remove();
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }

    /*
     * updates Environmental based on Environmental's environmentalId
     *
     * returns true if the Environmental's environmentalId is valid environmental
     * return false if the Environmental's environmentalId is not valid environmental
     */
    public boolean update(Environmental e) {
        if ((e == null) || (e.getEnvironmentalId() == null)) {
            return false;
        }
        if (valid(e.getEnvironmentalId())) {
            try {
                Vertex v = ProxStorGraph.instance.getVertex(e.getEnvironmentalId());
                v.setProperty("description", e.getDescription());
                v.setProperty("type", e.getType().toString());
                v.setProperty("typeIdentifier", e.getIdentifier());
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }

    /*
     * test Vertex for Environmental-ness
     */
    private boolean valid(Vertex... vertices) {
        for (Vertex v : vertices) {
            if ((v == null) || !v.getProperty("_type").equals("environmental")) {
                return false;
            }
        }
        return true;
    }

    /*
     * test environmentalId for Environmental-ness
     */
    private boolean valid(String... ids) {
        for (String id : ids) {
            try {
                if ((id == null) || !valid(ProxStorGraph.instance.getVertex(id))) {
                    return false;
                }
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    /*
     * returns Environmental stored under environmentalId
     *
     * returns null if:
     *   - environmentalId does not map to any graph vertex
     *   - vertex is not of type environmental
     *
     */
    private Environmental get(String environmentalId) {
        if (environmentalId == null) {
            return null;
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(environmentalId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        if ((v != null) && valid(v)) {
            return toEnvironmental(v);
        }
        return null;
    }
        
    /*
     * tests for validity of environmentalId conatined by locId
     *
     * returns false if:
     *   1 environmentalId does not map to any graph vertex
     *   2 locId does not map to any graph vertex
     *   3 environmentalId is not vertex of type Environmental
     *   4 locId is not vertex of type Location
     *   5 Location is not container of Environmental     
     */
    private boolean isLocationEnvironmental(String locId, String environmentalId) {       
       /*
        * this code protected by callers who already check validity
        */
        try {
            for (Edge e : ProxStorGraph.instance.getVertex(environmentalId).getEdges(IN, "contains")) {
                if (e.getVertex(OUT).getId().toString().equals(locId)) {
                    return true;
                }
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(EnvironmentalDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false; // condition 5        
    }

}
