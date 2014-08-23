package com.giannoules.proxstor.admin;

import com.giannoules.proxstor.admin.graph.GraphResource;
import javax.ws.rs.Path;

/*
 * /admin sub-resource locators returning resource class for various 
 *        sub-paths
 */

@Path("/admin")
public class AdminResource {
    
    /*
     * control running graph instance
     */
    @Path("/graph")
    public GraphResource getGraphResource() {
        return new GraphResource();
    }    
}
