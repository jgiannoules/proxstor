package com.giannoules.proxstor.user;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.InvalidUserId;
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
 * Data Access Object to database-persistent store of Users
 *
 * @TODO implement caching
 *
 * Currently uses only basic low-level Blueprints API
 *
 */
public enum UserDao {

    instance;

    private UserDao() {
    }

    /*
     * allow one or more userIds to be tested for validity
     *
     * return true iff all string params are valid userId, false otherwise
     *
     * no exceptions thrown. lack of database access silenty covered as false return
     *
     * used numerous places inside and outside .user package to validate userIDs
     */
    public boolean valid(String ... userIds) {
        if (userIds == null) {
            return false;
        }
        try {
            for (String id : userIds) {
                if (!UserDao.this.valid(ProxStorGraph.instance.getVertex(id))) {
                    return false;
                }
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    /*
     * test Vertex for User-ness
     *
     * returns true if Vertex is of type User, false otherwise
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
            if ((type == null) || (!type.equals("user"))) {
                return false;
            }
        }
        return true;
    }    

    /**
     * Returns User representation stored in back-end graph database under the 
     * specified Vertex object ID.
     *
     * <p>Used by UserResource @GET and numerous other places
     *
     * @param userId    The user id (object id) to used to to retrieve User
     * @return          User representation of Vertex user id, or null if unable to access database
     * @throws InvalidUserId    If the userId parameter is invalid
     */
    public User get(String userId) throws InvalidUserId {
        try {            
            Vertex v;
            v = ProxStorGraph.instance.getVertex(userId);
            if (!valid(v)) {
                throw new InvalidUserId();
            }
            return toUser(v);
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (ProxStorGraphNonExistentObjectID ex) {
            throw new InvalidUserId();
        }        
    }
    
    /*
     * returns User stored under Vertex v
     *
     * returns null if:
     *   - v is null     
     *   - vertex is not of type user
     *      
     * throws InvalidUserId if parameter is not valid id
     */
    public User get(Vertex v) throws InvalidUserId {
        if (UserDao.this.valid(v)) {
            return toUser(v);
        }
        throw new InvalidUserId();
    }
    
    /*
     * find all matching Users based on partially specified User
     *
     * returns all matching users as a collection, or null if there are no users
     * 
     * graph database not running becomes a null return
     *
     * used by SearchResource @POST
     */
    public Collection<User> get(User partial) {
        List<User> users = new ArrayList<>();
        if ((partial.getId() != null) && (!partial.getId().isEmpty())) {
            // invalid userID is not an exception, it is just no match condition
            try { 
                users.add(UserDao.this.get(partial.getId()));
                return users;
            } catch (InvalidUserId ex) {
                Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
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
            if (UserDao.this.valid(v)) {
                users.add(toUser(v));
            }
        }
        return users;
    }

    /* 
     * adds a new User to the Graph database
     *
     * returns User updated with actual userId used in the running
     * graph database instance
     *
     * returns null if unable to add User
     *
     * used by UsersResource @POST
     */
    public User add(User u) {
        if (u == null) {
            return null;
        }
        try {
            Vertex v = ProxStorGraph.instance.addVertex();
            v.setProperty("firstName", u.getFirstName());
            v.setProperty("lastName", u.getLastName());
            v.setProperty("email", u.getEmail());
            setType(v);
            ProxStorGraph.instance.commit();
            u.setId(v.getId().toString());
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
     * throws InvalidUserId if id is invalid
     *
     * used by UserResource @PUT
     */
    public boolean update(User u) throws InvalidUserId {
        validOrException(u.getId());                
        try {
            Vertex v = ProxStorGraph.instance.getVertex(u.getId());
            v.setProperty("firstName", u.getFirstName());
            v.setProperty("lastName", u.getLastName());
            v.setProperty("email", u.getEmail());
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException| ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }        
    }

    /* 
     * remove userId from graph
     *
     * returns true upon success
     * throws InvalidUserId if userId invalid
     *
     * used by UserResource @DELETE
     */
    public boolean delete(String userId) throws InvalidUserId {
        validOrException(userId);        
        try {
            ProxStorGraph.instance.getVertex(userId).remove();
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }        
    }

    public void validOrException(String ... ids) throws InvalidUserId {
        if (!valid(ids)) {
            throw new InvalidUserId();
        }
    }
    
    // ----> BEGIN private methods <----
    
    
    /*
     * converts vertex into User object
     *
     * (assumes sanity check already performed on vertex)
     *
     * returns non-null User if successful, otherwise null
     *
     * no exceptions thrown
     */
    private User toUser(Vertex v) {        
        if (v == null) {
            return null;
        }
        User u = new User();
        /*
         * note: getProperty() will return null for non-existent props
         */ 
        u.setFirstName((String) v.getProperty("firstName"));
        u.setLastName((String) v.getProperty("lastName"));
        u.setEmail((String) v.getProperty("email"));
        Object id = v.getId();
        if (id instanceof Long) {
            u.setId(Long.toString((Long) v.getId()));
        } else {
            u.setId(v.getId().toString());
        }
        return u;
    }
    
    /*
     * abstract away setting of Vertex User type to allow underlying graph
     * representation/management to evolve
     */
    private void setType(Vertex v) {
        if (v != null) {
            v.setProperty("_type", "user");
        }
    }
    
}
