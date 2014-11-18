package com.giannoules.proxstor.query;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Query;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.user.UserDao;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("query")
public class QueryResource {
    
    /*
     * returns all localities matching criteria in partially expressed User JSON
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMatchingLocalities(Query q) {
        long start = ProxStorDebug.startTimer();
        Collection<Locality> localities;  
        try {
            localities = QueryDao.instance.getMatching(q);
        } catch (InvalidUserId | InvalidLocationId ex) {
            Logger.getLogger(QueryResource.class.getName()).log(Level.SEVERE, null, ex);
            ProxStorDebug.endTimer("getMatchingLocalities404", start);
            return Response.status(404).build();
        }
        if (localities == null || localities.isEmpty()) {
            ProxStorDebug.endTimer("getMatchingLocalities204", start);
            return Response.noContent().build();
        }
        /*
         * ok() will not take Collection directly, so convert to array
         */
        ProxStorDebug.endTimer("getMatchingLocalities", start);
        return Response.ok((Locality[]) localities.toArray(new Locality[localities.size()])).build();
    }
}
