/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.giannoules.proxstor;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author James_Giannoules
 */
@Path("/users/{userid}")
public class UserResource {
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String getUser(@PathParam("userid") String userId) {
        return "{ userid " + userId + " coming your way }";
    }
    
    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String postUser(@PathParam("userid") String userId) {
        return "{ updating userid " + userId + " }";
    }
    
    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String deleteUser(@PathParam("userid") String userId) {
        return "{ deleting userid " + userId + " }";
    }
}
