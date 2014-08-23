package com.giannoules.proxstor.user;

import com.giannoules.proxstor.device.DeviceResource;
import com.giannoules.proxstor.knows.UserKnowsResource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class UserResource {

    private final String userId;

    /*
     * all these Paths assume a single userId context, so stash in constructor
     */
    public UserResource(String userId) {
        this.userId = userId;
    }

    /*
     * return DeviceResource handler for specified user
     */
    @Path("devices")
    public DeviceResource getDeviceResource() {
        return new DeviceResource(userId);
    }

    /*
     * return KnowResource handler for specified user
     */
    @Path("knows")
    public UserKnowsResource getKnowsResource() {
        return new UserKnowsResource(userId);
    }

    /*
     * return the specified userId User
     * 
     * success - return 200 (Ok) and JSON representation User
     * failure - return 404 (Not Found)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser() {
        User u = UserDao.instance.getUser(userId);
        if (u == null) {
            return Response.status(404).build();
        }
        return Response.ok().entity(u).build();
    }

    /*
     * update user
     * Note: userId path must match userId inside JSON (and be valid ID)
     * 
     * sucess - return 204 (No Content)
     * failure - return 404 (Not Found)
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putUser(User u) {
        if ((u.getUserId() != null) && u.getUserId().equals(userId)) {
            if (UserDao.instance.updateUser(u)) {
                return Response.noContent().build();
            }
        }
        return Response.status(404).build();
    }

    /*
     * remove userId from database
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)
     */
    @DELETE
    public Response deleteUser() {
        if (UserDao.instance.deleteUser(userId)) {
            return Response.noContent().build();
        } else {
            return Response.status(404).build();
        }
    }

}
