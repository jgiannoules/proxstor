package com.giannoules.proxstor.admin;

import com.giannoules.proxstor.admin.graph.GraphResource;
import com.giannoules.proxstor.admin.testing.TestingResource;
import javax.ws.rs.Path;

/**
 * admin sub-resource locators returning resource class for various 
 * sub-paths
 */

@Path("/admin")
public class AdminResource {
    
    /**
     * Returns an instance of GraphResource to handle graph/ path requests
     * 
     * @return GraphResource instance to handle request
     */
    @Path("/graph")
    public GraphResource getGraphResource() {
        return new GraphResource();
    }
    
    @Path("/testing")
    public TestingResource getTestingResource() {
        return new TestingResource();
    }
}
