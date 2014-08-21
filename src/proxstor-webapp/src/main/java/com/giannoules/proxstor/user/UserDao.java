package com.giannoules.proxstor.user;

import com.giannoules.proxstor.ProxStorGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        u.setAddress((String) v.getProperty("address"));
        Object id = v.getId();
         if (id instanceof Long) {            
            u.setUserId(Long.toString((Long) v.getId()));
         } else {
             u.setUserId(v.getId().toString());
         }
         return u;
    }
    
    /*
     * test Vertex for USer-ness
     */
    private boolean validUserVertex(Vertex v) {
        return (v != null) && v.getProperty("_type").equals("user");
    }

    /*
     * test user id for User-ness
     */
    // @TODO make this private in next commit
    public boolean validUserId(String userId) {
        return (userId != null) && validUserVertex(ProxStorGraph.instance.getVertex(userId));
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
        Vertex v = ProxStorGraph.instance.getVertex(userId);
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
        List<User> devices = new ArrayList<>();
        for (Vertex v : ProxStorGraph.instance.getGraph().getVertices("_type", "user")) {
            devices.add(vertexToUser(v));
        }
        return devices;
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
        Vertex v = ProxStorGraph.instance.newVertex();
        v.setProperty("firstName", u.getFirstName());
        v.setProperty("lastName", u.getLastName());
        v.setProperty("address", u.getAddress());
        setVertexToUserType(v);
        ProxStorGraph.instance.commit();
        u.setUserId(v.getId().toString());
        return u;        
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
            Vertex v = ProxStorGraph.instance.getVertex(u.getUserId());
            v.setProperty("firstName", u.getFirstName());
            v.setProperty("lastName", u.getLastName());
            v.setProperty("address", u.getAddress());
            ProxStorGraph.instance.commit();
            return true;
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
            ProxStorGraph.instance.getVertex(userId).remove();
            ProxStorGraph.instance.commit();
            return true;
        }
        return false;
    }

}
