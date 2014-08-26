package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum KnowsDao {

    instance;

    /*
     * returns all Users userId *knows*
     *
     * returns null if:
     *   - userId is not valid User
     */
    public Collection<User> getUserKnowsStrength(String userId, Integer strength) {
        if ((userId != null) && UserDao.instance._validUserId(userId) && (strength != null)) {
            List<User> knows = new ArrayList<>();
            Vertex v;
            try {
                v = ProxStorGraph.instance.getVertex(userId);
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            for (Edge e : v.getEdges(OUT, "knows")) {
                Integer knowsStrength = (Integer) e.getProperty("strength");
                ProxStorDebug.println("KnowsDao.getUserKnows(): retrieved property strength is : " + knowsStrength);
                if ((knowsStrength != null) && (knowsStrength >= strength)) {
                    knows.add(UserDao.instance.getUser(e.getVertex(IN)));
                }
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
    public Collection<User> getKnowsUserStrength(String userId, Integer strength) {
        if ((userId != null) && UserDao.instance._validUserId(userId) && (strength != null)) {
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
