package com.giannoules.proxstor.within;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.LocationAlreadyWithinLocation;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.location.LocationDao;
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

public enum WithinDao {
    
    instance;

    public Collection<Location> getWithin(String locId) throws InvalidLocationId {
        LocationDao.instance.validOrException(locId);
        VertexQuery vq;
        try {
            vq = ProxStorGraph.instance.getVertex(locId).query();
            vq.direction(OUT);
            vq.labels("within");
            List<Location> locations = new ArrayList<>();
            for (Vertex v : vq.vertices()) {
                locations.add(LocationDao.instance.get(v));
            }
            return locations;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(WithinDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public Collection<Location> getContaining(String locId) throws InvalidLocationId {
        LocationDao.instance.validOrException(locId);
        VertexQuery vq;
        try {
            vq = ProxStorGraph.instance.getVertex(locId).query();
            vq.direction(IN);
            vq.labels("within");
            List<Location> locations = new ArrayList<>();
            for (Vertex v : vq.vertices()) {
                locations.add(LocationDao.instance.get(v));
            }
            return locations;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(WithinDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /*
     *
     * @TODO don't recreate within if already exists
     */
    public boolean addWithin(String innerLocId, String outerLocId) throws InvalidLocationId, LocationAlreadyWithinLocation {
        LocationDao.instance.validOrException(innerLocId, outerLocId);        
        if (locationWithinLocation(innerLocId, outerLocId)) {
            throw new LocationAlreadyWithinLocation();
        }
        try {
            Vertex outVertex = ProxStorGraph.instance.getVertex(innerLocId);
            Vertex inVertex = ProxStorGraph.instance.getVertex(outerLocId);
            ProxStorGraph.instance.addEdge(outVertex, inVertex, "within").setProperty("_target", outerLocId);
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /*
     * removes an established Within relationship from in -> out
     *
     * returns true if succesful
     * returns false if either locId is invalid or if a Within relationship
     * was not already established 
     */
    public boolean removeWithin(String innerLocId, String outerLocId) throws InvalidLocationId  {
        if (!locationWithinLocation(innerLocId, outerLocId)) {
            return false;
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(innerLocId);
            VertexQuery vq = v.query();
            vq.direction(OUT);
            vq.labels("within");
            vq.has("_target", outerLocId);
            for (Edge e : vq.edges()) {
                e.remove();
                ProxStorGraph.instance.commit();
                return true;
            }
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }       
    }
   
    /*
     * tests for location within relationship innerLocId -> outerLocId
     *
     * does not currently test further down the (possible) link
     *
     * @TODO implement Gremlin search following arbitrary number of within
     *       edges
     */
    public boolean locationWithinLocation(String innerLocId, String outerLocId) throws InvalidLocationId {
        LocationDao.instance.validOrException(innerLocId, outerLocId);
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(innerLocId);
            VertexQuery vq = v.query();
            vq.direction(OUT);
            vq.labels("within");
            vq.has("_target", outerLocId);
            return (vq.count() == 1);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(WithinDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }                
    }    
    
}
