package com.giannoules.proxstor.within;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.LocationAlreadyWithinLocation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

 public class WithinLocationResource {
    private final String locId;
    private final String locId2;
    
     public WithinLocationResource(String locId, String locId2) {
         this.locId = locId;
         this.locId2 = locId2;
     }
     
     @GET
     public Response getLocationWithinLocaton() {
        try {
            System.out.println("1");
            if (WithinDao.instance.locationWithinLocation(locId, locId2)) {
                System.out.println("2");
                return Response.noContent().build();
            }
        } catch (InvalidLocationId ex) {
            Logger.getLogger(WithinLocationResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("2*");
        return Response.status(404).build();
     }
     
     @POST
     public Response establishLocationWithin() {
         try {
             if (!WithinDao.instance.addWithin(locId, locId2)) {
                 return Response.status(500).build();
             }
             URI createdUri;
             try {
                 createdUri = new URI("/locations/" + locId + "/within/" + locId2);
                 return Response.created(createdUri).build();
             } catch (URISyntaxException ex) {
                 Logger.getLogger(WithinLocationResource.class.getName()).log(Level.SEVERE, null, ex);
                 return Response.serverError().build();
             }
         } catch (InvalidLocationId ex) {
             Logger.getLogger(WithinLocationResource.class.getName()).log(Level.SEVERE, null, ex);
             return Response.status(404).build();
         } catch (LocationAlreadyWithinLocation ex) {
             Logger.getLogger(WithinLocationResource.class.getName()).log(Level.SEVERE, null, ex);
             return Response.status(400).build();
         }
     }
     
     @DELETE
     public Response deleteLocationWithinLocation() {
         try {
             WithinDao.instance.removeWithin(locId, locId2);
             return Response.noContent().build();
         } catch (InvalidLocationId ex) {
             Logger.getLogger(WithinLocationResource.class.getName()).log(Level.SEVERE, null, ex);
         }
         return Response.status(404).build();
     }
}
