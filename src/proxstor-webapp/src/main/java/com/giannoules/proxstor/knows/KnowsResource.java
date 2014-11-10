package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.UserAlreadyKnowsUser;
import com.giannoules.proxstor.api.User;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/*
 * handles all URI requests regarding 'knows' relationship between users
 */
public class KnowsResource {

    private final String userIdA;        // always within context of a user
    private final Integer strengthVal;  // strength meaning varies by request

    public KnowsResource(String userId, Integer strength) {
        this.userIdA = userId;
        this.strengthVal = strength;
    }

    /*
     * return all Users which userId knows with strength >= strengthVal
     *
     * returns 200 (Ok) with array of Users if matches found
     * returns 204 (No Content) if no knows relationships match criteria
     * returns 404 (Not Found) if userId is invalid
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnownUsers() {
        Collection<User> users;
        try {
            users = KnowsDao.instance.getUserKnows(userIdA, strengthVal, OUT, 1024); // max 1024 users returned
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
        if (users == null) {
            return Response.noContent().build();
        }
        return Response.ok((User[]) users.toArray(new User[users.size()])).build();
    }

    /*
     * returns all Users who know userId with at least minimum strength
     *
     * returns 204 (No Content) if no knows relatioships match
     * returns 200 (Ok) with array of Users if matches found
     * returns 404 (Not Found) if userId is invalid
     */
    @Path("reverse")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnowsUsers() {
        Collection<User> users;
        try {
            users = KnowsDao.instance.getUserKnows(userIdA, strengthVal, IN, 1024); // max 1024 users returned
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
        if (users == null) {
            return Response.noContent().build();
        }
        return Response.ok((User[]) users.toArray(new User[users.size()])).build();
    }

    @Path("user/{otheruser}")
    public KnowsUserResource returnKnowsUserResource(@PathParam("otheruser") String otherUser) {
        return new KnowsUserResource(userIdA, otherUser, strengthVal);
    }
    
}
