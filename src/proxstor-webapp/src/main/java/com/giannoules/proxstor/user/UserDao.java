package com.giannoules.proxstor.user;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.ProxStorGraphNonExistentObjectID;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Data Access Object to database-persistent store of Users
 *
 * @TODO implement caching
 *
 * Currently uses basic low-level Blueprints API
 *
 */
public enum UserDao {

    instance;

    private UserDao() {
    }

    /*
     * converts vertex into User object
     *
     * assumes sanity check already performed on vertex
     */
    private User vertexToUser(Vertex v) {
        if (v == null) {
            return null;
        }
        User u = new User();
        u.setFirstName((String) v.getProperty("firstName"));
        u.setLastName((String) v.getProperty("lastName"));
        u.setEmail((String) v.getProperty("email"));
        Object id = v.getId();
        if (id instanceof Long) {
            u.setUserId(Long.toString((Long) v.getId()));
        } else {
            u.setUserId(v.getId().toString());
        }
        return u;
    }

    /*
     * test Vertex for User-ness
     * @TODO .getProperty might return null.. fix this to avoid NullPointerException
     */
    private boolean validUserVertex(Vertex v) {
        return (v != null) && v.getProperty("_type").equals("user");
    }

    /*
     * test user id for User-ness
     */
    private boolean validUserId(String userId) {
        try {
            return (userId != null) && validUserVertex(ProxStorGraph.instance.getVertex(userId));
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /*
     * abstract away setting of Vertex User type
     */
    private void setVertexToUserType(Vertex v) {
        if (v != null) {
            v.setProperty("_type", "user");
        }
    }

    /*
     * returns User stored under userId
     *
     * returns null if:
     *   - userId does not map to any graph vertex
     *   - vertex is not of type user
     *
     */
    public User getUser(String userId) {
        if (userId == null) {
            return null;
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(userId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        if ((v != null) && validUserVertex(v)) {
            return vertexToUser(v);
        }
        return null;
    }

    /*
     * returns User stored under Vertex v
     *
     * returns null if:
     *   - v is null     
     *   - vertex is not of type user
     *
     */
    public User getUser(Vertex v) {
        if ((v != null) && validUserVertex(v)) {
            return vertexToUser(v);
        }
        return null;
    }

    /*
     * returns all Users in database
     */
    public Collection<User> getAllUsers() {
        try {
            List<User> devices = new ArrayList<>();
            for (Vertex v : ProxStorGraph.instance.getVertices("_type", "user")) {
                devices.add(vertexToUser(v));
            }
            return devices;
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /*
     * returns all Users userId *knows*
     *
     * returns null if:
     *   - userId is not valid User
     */
    public List<User> getUserKnows(String userId) {
        if ((userId != null) && validUserId(userId)) {
            List<User> knows = new ArrayList<>();
            Vertex v;
            try {
                v = ProxStorGraph.instance.getVertex(userId);
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            for (Edge e : v.getEdges(OUT, "knows")) {
                knows.add(UserDao.instance.getUser(e.getVertex(IN)));
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
    public List<User> getKnowsUser(String userId) {
        if ((userId != null) && validUserId(userId)) {
            List<User> knows = new ArrayList<>();
            Vertex v;
            try {
                v = ProxStorGraph.instance.getVertex(userId);
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            for (Edge e : v.getEdges(IN, "knows")) {
                knows.add(UserDao.instance.getUser(e.getVertex(OUT)));
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

    public boolean addKnows(String fromUser, String toUser) {
        if (validUserId(fromUser) && validUserId(toUser)) {
            try {
                Vertex out = ProxStorGraph.instance.getVertex(fromUser);
                Vertex in = ProxStorGraph.instance.getVertex(toUser);
                ProxStorGraph.instance.addEdge(out, in, "knows");
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException| ProxStorGraphNonExistentObjectID ex) {
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
        if (validUserId(fromUser) && validUserId(toUser)) {
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

    /* 
     * adds a new User to the Graph database
     *
     * returns User updated with actual userId used in the running
     * graph database instance
     */
    public User addUser(User u) {
        if (u == null) {
            return null;
        }
        try {
            Vertex v = ProxStorGraph.instance.addVertex();
            v.setProperty("firstName", u.getFirstName());
            v.setProperty("lastName", u.getLastName());
            v.setProperty("email", u.getEmail());
            setVertexToUserType(v);
            ProxStorGraph.instance.commit();
            u.setUserId(v.getId().toString());
            return u;
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /*
     * updates User based on User's userId
     *
     * returns true if valid userId
     * returns false is userId invalid
     */
    public boolean updateUser(User u) {
        if ((u == null) || (u.getUserId() == null)) {
            return false;
        }
        if (validUserId(u.getUserId())) {
            try {
                Vertex v = ProxStorGraph.instance.getVertex(u.getUserId());
                v.setProperty("firstName", u.getFirstName());
                v.setProperty("lastName", u.getLastName());
                v.setProperty("email", u.getEmail());
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException| ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /* 
     * remove userId from graph
     *
     * returns true upon success
     * returns false if userId was not a User
     */
    public boolean deleteUser(String userId) {
        if ((userId != null) && (validUserId(userId))) {
            try {
                ProxStorGraph.instance.getVertex(userId).remove();
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

}
