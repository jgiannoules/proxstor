package com.giannoules.proxstor.environmental;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidParameter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EnvironmentalsResource {

    private final String locId;

    /*
     * all these Paths assume locId context, so stash in constructor
     */
    public EnvironmentalsResource(String locId) {
        this.locId = locId;
    }

    /*
     * return the specific lcdId's Environmentals
     * 
     * success - return 200 (Ok) and JSON representation Environmental
     * success - return 204 (No Content) if locId has no Environmentals
     * failure - return 404 (Not Found) if locId is invalid     
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLocationEnvironmentals() {
        Collection<Environmental> environmentals;
        try {
            environmentals = EnvironmentalDao.instance.getAllLocationEnvironmentals(locId);
            
            /*
             * changing to return emtpy list for no environmentals insted of 204
             */    
            
//            if (environmentals.isEmpty()) {
//                return Response.noContent().build();
//            }
            
            /*
             * ok() will not take Collection directly, so convert to array
             */
            return Response.ok((Environmental[]) environmentals.toArray(new Environmental[environmentals.size()])).build();
        } catch (InvalidLocationId ex) {
            Logger.getLogger(EnvironmentalsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }

    }

    /*
     * adds environmentals to database contained in locId
     *
     * returns instance of added Environmental
     *
     * success - returns 201 (Created) with URI of new Environmental and Environmental JSON in the body
     * failure - returns 400 (Bad Request) if the Environmental could not be added
     *           returns 500 (Server Error) if the Environmental could was added and 
     *                                      URI building error occurred
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postLocationEnvironmental(Environmental in) {
        Environmental e;
        try {
            e = EnvironmentalDao.instance.add(locId, in);
            if (e == null) {
                return Response.status(400).build();
            } else {
                try {
                    URI createdUri = new URI("locations/" + locId + "/environmentals/" + e.getEnvironmentalId());
                    return Response.created(createdUri).entity(e).build();
                } catch (URISyntaxException ex) {
                    Logger.getLogger(EnvironmentalsResource.class.getName()).log(Level.SEVERE, null, ex);
                    return Response.serverError().build();
                }
            }
        } catch (InvalidLocationId ex) {
            Logger.getLogger(EnvironmentalsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (InvalidParameter ex) {
            Logger.getLogger(EnvironmentalsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }
    
    // ---- BEGIN sub-resource locators ----
    
    /*
     * return EnvironmentalResource handler for specified environmentalId
     */
    @Path("{environmentalid}")
    public EnvironmentalResource getEnvironmentalResource(@PathParam("environmentalid") String environmentalId) {
        ProxStorDebug.println("getEnvironmentalResource(" + environmentalId + ")");
        return new EnvironmentalResource(locId, environmentalId);
    }
}
