package com.giannoules.proxstor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/testing")
public class NewClass {
    
    @GET
    @Path("accepted")
    public Response accepted() {
        return Response.accepted().build();        
    }
    
    @GET
    @Path("notacceptable")
    public Response notAcceptable() {
        return Response.notAcceptable(null).build();
    }
    
    
}
