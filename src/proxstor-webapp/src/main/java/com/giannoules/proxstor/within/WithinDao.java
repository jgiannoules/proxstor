package com.giannoules.proxstor.within;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.location.LocationDao;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WithinDao {
    
     /*
     *
     * @TODO don't recreate within if already exists
     */
    public boolean establishLocationWithinLocation(String innerLocId, String outerLocId) throws InvalidLocationId {            
        if (!LocationDao.instance.valid(innerLocId) || !LocationDao.instance.valid(outerLocId)) {
            throw new InvalidLocationId();
        }
        if (locationWithinLocation(innerLocId, outerLocId)) {
            ProxStorDebug.println("Caught you!");
        }
        try {
            Vertex outVertex = ProxStorGraph.instance.getVertex(innerLocId);
            Vertex inVertex = ProxStorGraph.instance.getVertex(outerLocId);
            ProxStorGraph.instance.addEdge(outVertex, inVertex, "within");
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
    public boolean removeLocationWithinLocation(String innerLocId, String outerLocId) throws InvalidLocationId {
        if (!LocationDao.instance.valid(innerLocId) || !LocationDao.instance.valid(outerLocId)) {
            throw new InvalidLocationId();
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(innerLocId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        for (Edge e : v.getEdges(OUT, "within")) {
            if (e.getVertex(IN).getId().toString().equals(outerLocId)) {
                e.remove();
                return true;
            }
        }
        return false;
    }
   
    public boolean locationWithinLocation(String innerLocId, String outerLocId) {
        List<Vertex> vertices = null;
        try {
             vertices = ProxStorGraph.instance.getVertices(innerLocId, outerLocId, OUT, "within");
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(LocationDao.class.getName()).log(Level.SEVERE, null, ex);             
        }
        return ((vertices != null) && (!vertices.isEmpty()));
    }
    
    
}
