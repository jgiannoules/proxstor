package com.giannoules.proxstor.user;

import com.giannoules.proxstor.ProxStorGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    
    public User vertexToUser(Vertex v) {
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

    public User getUser(String userId) {
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        if (v != null) {
            return vertexToUser(v);
        }
        return null;
    }

    public Collection<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for (Vertex v : ProxStorGraph.instance.getGraph().getVertices("_type", "user")) {            
            users.add(vertexToUser(v));
        }
        return users;
    }

    public User addUser(User u) {
        if (u.getUserId() == null) {
            User newUser = new User(u.getFirstName(), u.getLastName(), u.getAddress());
            Vertex v = ProxStorGraph.instance.newVertex();
            v.setProperty("firstName", newUser.getFirstName());
            v.setProperty("lastName", newUser.getLastName());
            v.setProperty("address", newUser.getAddress());
            v.setProperty("_type", "user");
            ProxStorGraph.instance.commit();
            newUser.setUserId(v.getId().toString());
            return newUser;
        } else {
            return null;
        }
    }

    public boolean updateUser(User u) {
        Vertex v = ProxStorGraph.instance.getVertex(u.getUserId());
        if (v != null) {            
            v.setProperty("firstName", u.getFirstName());
            v.setProperty("lastName", u.getLastName());
            v.setProperty("address", u.getAddress());
            ProxStorGraph.instance.commit();
            return true;
        }
        return false;
    }

    public boolean deleteUser(String userId) {
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        if (v != null) {
            v.remove();
            ProxStorGraph.instance.commit();
            return true;
        }
        return false;
    }

    public boolean validUserId(String userId) {
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        return (v != null) && (v.getProperty("_type").equals("user"));
    }
}