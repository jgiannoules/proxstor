package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidModel;
import com.giannoules.proxstor.exception.LocationAlreadyNearbyLocation;
import com.giannoules.proxstor.nearby.NearbyDao;
import com.tinkerpop.blueprints.Edge;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

public class NearbyLocationResource {
    private final String locIdA;
    private final String locIdB;
    private final Integer distanceVal;

    public NearbyLocationResource(String locIdA, String locIdB, Integer distanceVal) {
        this.locIdA = locIdA;
        this.locIdB = locIdB;
        this.distanceVal = distanceVal;
    }
    
    /*
     * test whether locA is within distance from locB
     */
    @GET
    public Response getLocationsWithinDistance() {
        try {
            try {
                Edge e = NearbyDao.instance.getNearby(locIdA, locIdB);
                Integer distance = e.getProperty("distance");
                if ((distance != null) && (distance <= distanceVal)) {
                    return Response.noContent().build();
                }
            } catch (InvalidModel ex) {
                Logger.getLogger(NearbyLocationResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (InvalidLocationId ex) {
            Logger.getLogger(NearbyLocationResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();
    }
    
    /*
     * establish the nearby relationship between locations with distance
     *
     * success - returns 201 (Created) with URI of new Nearby relationship
     * failure - returns 404 (Not Found) if either locID is invalid
     *           returns 400 (Bad Request) if the Nearby is already established
     *           returns 500 (Server Error) if the Nearby could be established 
     *                                      but URI building error occurred
     */    
    @POST
    public Response establishLocationNearby() {
        try {
            if (!NearbyDao.instance.addNearby(locIdA, locIdB, distanceVal)) {
                return Response.status(500).build();
            }
        } catch (InvalidLocationId ex) {
            Logger.getLogger(NearbyLocationResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (LocationAlreadyNearbyLocation ex) {
            Logger.getLogger(NearbyLocationResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
        URI createdUri;
        try {
            createdUri = new URI("/locations/" + locIdA + "/nearby/" + distanceVal.toString() + "/" + locIdB);
            return Response.created(createdUri).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(NearbyLocationResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }

    /*
     * update distance of nearby relationship between locations
     *
     * success - returns 204 (No Content)
     * failure - returns 404 (Not Found) if either locID is invalid
     *           returns 400 (Bad Request) if the nearby relationship was not already established
     */    
    @PUT
    public Response updateLocationNearby() {
        try {
            if (NearbyDao.instance.updateNearby(locIdA, locIdB, distanceVal)) {
                return Response.noContent().build();
            } else {
                return Response.status(400).build();
            }
        } catch (InvalidLocationId ex) {
            Logger.getLogger(NearbyLocationResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }        
    }

    /*
     * remove the nearby relationship between locations
     *
     * note that distanceVal is ignored
     *
     * returns 204 (No Content) when successful
     * returns 404 (Not Found) if relationship was not already established or
     *                         the locIds are simply invalid
     */    
    @DELETE
    public Response removeLocationNearby() {
        try {
            if (NearbyDao.instance.removeNearby(locIdA, locIdB)) {
                return Response.noContent().build();
            }
        } catch (InvalidLocationId ex) {
            Logger.getLogger(NearbyLocationResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();        
    }
}
