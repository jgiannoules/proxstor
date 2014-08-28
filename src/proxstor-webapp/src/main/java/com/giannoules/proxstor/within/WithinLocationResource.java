package com.giannoules.proxstor.within;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

 public class WithinLocationResource {
    private final String locId;
    private final String locId2;
    
     public WithinLocationResource(String locId, String locId2) {
         this.locId = locId;
         this.locId2 = locId;
     }
     
     @POST
     public Response postLocationWithinLocation() {
         return null;
     }
     
     @DELETE
     public Response deleteLocationWithinLocation() {
         return null;
     }
}
