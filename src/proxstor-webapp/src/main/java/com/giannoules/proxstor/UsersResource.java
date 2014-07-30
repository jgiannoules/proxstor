/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.giannoules.proxstor;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author James_Giannoules
 */
@Path("/users")
public class UsersResource {
  
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String getUsers() {
        return "{ all users coming your way }";
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String postUser() {
        return "{ adding user }";
    }
}
