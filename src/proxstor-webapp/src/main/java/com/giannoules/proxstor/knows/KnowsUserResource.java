package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/*
 * handles answering requests regarding *who knows userId*
 */
public class KnowsUserResource {
    
    private final String userId;
    
    public KnowsUserResource(String userId) {
        this.userId = userId;
    }
    
    /*
     * return all Users who know userId
     */ 
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getKnowsUsers() {
        return UserDao.instance.getKnowsUser(userId);
    }
}
