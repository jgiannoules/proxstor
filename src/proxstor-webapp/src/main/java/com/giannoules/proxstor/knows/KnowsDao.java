package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.InvalidUserID;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Compare.GREATER_THAN_EQUAL;
import com.tinkerpop.blueprints.Direction;
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

public enum KnowsDao {

    instance;

    /*
     * returns all users with knows relationship to userId 
     * results controlled by:
     *   - with strength >= strengthVal
     *   - with Direction direction controlling directionality of relationship
     *   - results are limited to a max of limit
     *
     * returns null if:
     *   - no matches found
     *
     * throws InvalidUserID if the userID is invalid
     */
    public Collection<User> getUserKnows(String userId, Integer strengthVal, Direction direction, int limit) throws InvalidUserID {
        if ((userId != null) && (strengthVal != null)) {            
           if (!UserDao.instance._validUserId(userId)) {
                throw new InvalidUserID();
            }
            List<User> knows = new ArrayList<>();
            try {
                VertexQuery vq = ProxStorGraph.instance.getVertex(userId).query();
                vq.direction(direction);
                vq.labels("knows");
                vq.has("strength", GREATER_THAN_EQUAL, strengthVal);
                vq.limit(limit);
                for (Vertex v : vq.vertices()) {
                    knows.add(UserDao.instance.getUser(v));
                }
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return knows;
        }
        return null;
    }

    /*
     * returns all *Users who know* userId
     *
     * returns null if:
     *   - userId is not valid User
     */
    public Collection<User> getKnowsUserStrength(String userId, Integer strength) throws InvalidUserID {
        if ((userId != null) && (strength != null)) {
            if (!UserDao.instance._validUserId(userId)) {
                throw new InvalidUserID();
            }            
            List<User> knows = new ArrayList<>();
            Vertex v;
            try {
                v = ProxStorGraph.instance.getVertex(userId);
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            for (Edge e : v.getEdges(IN, "knows")) {
                Integer knowsStrength = (Integer) e.getProperty("strength");
                if ((knowsStrength != null) && (knowsStrength >= strength)) {
                    knows.add(UserDao.instance.getUser(e.getVertex(IN)));
                }
            }
            return knows;
        }
        return null;
    }

    /*
     * establish a Knows relationship from fromUser -> toUser
     *
     * returns true if both IDs are valid Users, false otherwise
     */
    public boolean addKnows(String fromUser, String toUser, Integer strength) {
        if (UserDao.instance._validUserId(fromUser) && UserDao.instance._validUserId(toUser) && (strength != null)) {
            try {
                Vertex out = ProxStorGraph.instance.getVertex(fromUser);
                Vertex in = ProxStorGraph.instance.getVertex(toUser);
                ProxStorGraph.instance.addEdge(out, in, "knows").setProperty("strength", strength);
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    /*
     * updates a Knows relationship from fromUser -> toUser
     *
     * returns true if both IDs are valid Users, false otherwise
     *
     * @TODO check for existing relationship. currently the method is identical to addKnows. created duplicates.
     */
    public boolean updateKnows(String fromUser, String toUser, Integer strength) {
        if (UserDao.instance._validUserId(fromUser) && UserDao.instance._validUserId(toUser) && (strength != null)) {
            try {
                if (!ProxStorGraph.instance.getVertices(fromUser, toUser, OUT, "knows").isEmpty()) {
                    ProxStorDebug.println("Caught you double dipping");
                }
                Vertex out = ProxStorGraph.instance.getVertex(fromUser);
                Vertex in = ProxStorGraph.instance.getVertex(toUser);
                ProxStorGraph.instance.addEdge(out, in, "knows").setProperty("strength", strength);
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /*
     * removes an established Knows relationship from fromUser -> toUser
     *
     * returns true if succesful
     * returns false if either user ID is invalid or if a Knows relationship
     * was not already established fromUser -> toUser
     */
    public boolean removeKnows(String fromUser, String toUser) {
        if (UserDao.instance._validUserId(fromUser) && UserDao.instance._validUserId(toUser)) {
            Vertex v;
            try {
                v = ProxStorGraph.instance.getVertex(fromUser);
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            for (Edge e : v.getEdges(OUT, "knows")) {
                if (e.getVertex(IN).getId().toString().equals(toUser)) {
                    e.remove();
                    return true;
                }
            }
        }
        return false;
    }

}
