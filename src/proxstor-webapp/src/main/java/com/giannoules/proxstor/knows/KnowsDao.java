package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.InvalidModel;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.exception.UserAlreadyKnowsUser;
import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Compare.GREATER_THAN_EQUAL;
import com.tinkerpop.blueprints.Direction;
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

    /**
     * Returns all users with knows relationship to userId 
     * results controlled by:
     * <ul>
     *   <li>with strength >= strengthVal
     *   <li>with Direction direction controlling directionality of relationship
     *   <li>results are limited to a max of limit
     *</ul>
     * @param userId
     * @param strengthVal
     * @param direction
     * @param limit
     * @returns Collection of User objects matching criteria, or null if no matches found
     * @throws InvalidUserId If the userID is invalid
     */
    public Collection<User> getUserKnows(String userId, Integer strengthVal, Direction direction, int limit) throws InvalidUserId {
        if ((userId != null) && (strengthVal != null)) {
            if (!UserDao.instance.valid(userId)) {
                throw new InvalidUserId();
            }
            List<User> knows = new ArrayList<>();
            try {
                VertexQuery vq = ProxStorGraph.instance.getVertex(userId).query();
                vq.direction(direction);
                vq.labels("knows");
                vq.has("strength", GREATER_THAN_EQUAL, strengthVal);
                vq.limit(limit);                               
                for (Vertex v : vq.vertices()) {
                    knows.add(UserDao.instance.get(v));
                }
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(KnowsDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return knows;
        }
        return null;
    }

    /*
     * establish a Knows relationship from fromUser -> toUser
     *
     * returns true if both IDs are valid Users
     * returns false if strength is null, InvalidModel caught, database is not
     *                running, or if the IDs provided are invalid
     * throws InvalidUserId if the fromUser or toUser IDs are not valid IDs
     * throws UserAlreadyKnowsUser if the knows relationship was already
     *                             established from fromUser -> toUser
     */
    public boolean addKnows(String fromUser, String toUser, Integer strength) throws InvalidUserId, UserAlreadyKnowsUser {        
        if (strength == null) {
            return false;
        }
        try {
            if (getKnows(fromUser, toUser) != null) {
                throw new UserAlreadyKnowsUser();
            }
        } catch (InvalidModel ex) {
            return false;
        }
        Vertex out;
        Vertex in;
        Edge e;
        try {
            out = ProxStorGraph.instance.getVertex(fromUser);
            in = ProxStorGraph.instance.getVertex(toUser);
            e = ProxStorGraph.instance.addEdge(out, in, "knows");
            e.setProperty("strength", strength);
            /* 
             * temporary implementation to speed up Blueprints VertexQuery
             * using Gremlin later will remove the need for this
             */
            e.setProperty("_target", toUser);         
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(KnowsDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
        return true;
    }


    /*
     * retrieves Knows relationship (Edge) from fromUser to toUser
     *
     * returns Edge if found
     * returns null if not found
     * throws InvalidUserId if either user id is invalid
     * throws InvalidModel if multiple knows relationships found between users
     */
    public Edge getKnows(String fromUser, String toUser) throws InvalidUserId, InvalidModel {
        if (!UserDao.instance.valid(fromUser, toUser)) {
            throw new InvalidUserId();
        }
        try {
            // this is painful without Gremlin
            VertexQuery vq = ProxStorGraph.instance.getVertex(fromUser).query();
            vq.direction(OUT);
            vq.labels("knows");
            vq.has("_target", toUser);
            long c = vq.count();
            if (c == 1) {
                return vq.edges().iterator().next();
            }
            if (c > 1) {
                throw new InvalidModel();

            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(KnowsDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /*
     * updates a Knows relationship from fromUser -> toUser
     *
     * returns true if both IDs are valid Users, false otherwise
     *
     * @TODO check for existing relationship. currently the method is identical to addKnows. created duplicates.
     */
    public boolean updateKnows(String fromUser, String toUser, Integer strength) throws InvalidUserId {
        Edge e;
        try {
            e = getKnows(fromUser, toUser);
        } catch (InvalidModel ex) {
            return false;
        }
        if (e != null) {
            e.setProperty("strength", strength);
            return true;
        }
        return false;
    }

    /*
     * removes an established Knows relationship from fromUser -> toUser
     *
     * returns true if succesful
     * returns false if a Knows relationship was not already established fromUser -> toUser
     * throws InvalidUSerID() if either userID is invalid
     */
    public boolean removeKnows(String fromUser, String toUser) throws InvalidUserId {
        Edge e;
        try {
            e = getKnows(fromUser, toUser);
            if (e != null) {
                e.remove();
                return true;

            }
        } catch (InvalidModel ex) {
            Logger.getLogger(KnowsDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
