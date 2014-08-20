package com.giannoules.proxstor.admin;

import com.giannoules.proxstor.admin.graph.GraphResource;
import javax.ws.rs.Path;

@Path("/admin")
public class AdminResource {
    
    @Path("/graph")
    public GraphResource getGraphResource() {
        return new GraphResource();
    }    
}
