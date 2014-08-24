package com.giannoules.proxstor.user;

import com.giannoules.proxstor.ProxStorGraph;
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
    public boolean _validUserId(String userId) {
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
     * find all matching Users based on partially specified User
     */
    public Collection<User> getMatchingUsers(User partial) {
        List<User> users = new ArrayList<>();
        if ((partial.getUserId() != null) && (!partial.getUserId().isEmpty())) {
            users.add(getUser(partial.getUserId()));
            return users;
        }
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "user");
        if ((partial.getFirstName() != null) && (!partial.getFirstName().isEmpty())) {
            q.has("firstName", partial.getFirstName());
        }
        if ((partial.getLastName() != null) && (!partial.getLastName().isEmpty())) {
            q.has("lastName", partial.getLastName());
        }
        if ((partial.getEmail() != null) && (!partial.getEmail().isEmpty())) {
            q.has("email", partial.getEmail());
        }
        for (Vertex v : q.vertices()) {
            if (validUserVertex(v)) {
                users.add(vertexToUser(v));
            }
        }
        return users;
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
        if (_validUserId(u.getUserId())) {
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
        if ((userId != null) && (_validUserId(userId))) {
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
