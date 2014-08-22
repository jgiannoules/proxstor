package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/*
 * handles answering requests regarding *who userId knows*
 */
public class UserKnowsResource {

    private final String userId;
    
    /*
     * store userId
     */
    public UserKnowsResource(String userId) {
        this.userId = userId;
    }
    
    /*
     * get all Users userId knows
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUserKnows() {
        return UserDao.instance.getUserKnows(userId);
    }

    /*
     * establish a Knows relationship between userId->knowsId
     */
    @Path("{knowsid: [0-9]+}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public boolean postUserKnows(@PathParam("knowsid") String knowsId) {
        return UserDao.instance.addKnows(userId, knowsId);
    }

    /*
     * break knows relationship between userId -> knowNoMoreId
     */
    @Path("{knownomoreid: [0-9]+}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public boolean deleteUserKnows(@PathParam("knownomoreid") String knowNoMoreId) {
        return UserDao.instance.removeKnows(userId, knowNoMoreId);
    }
}
