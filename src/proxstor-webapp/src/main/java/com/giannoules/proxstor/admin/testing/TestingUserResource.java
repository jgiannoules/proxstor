package com.giannoules.proxstor.admin.testing;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.user.UserDao;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TestingUserResource {

    static Integer NUM_THREADS = 16;

    /**
     * generate requested number of users and associate one unique device with
     * each. users are randomly associated with a 'knows' relationship to
     * previously created users
     *
     * @param count
     * @return
     */
    @Path("/generate/{count}")
    @POST
    public Response generateUsers(@PathParam("count") Integer count) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            Runnable worker = new UserWorker(count / NUM_THREADS);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return Response.noContent().build();
    }

    /**
     * using reservoir sampling return count users randomly selected from
     * database
     *
     * @param count
     * @return
     */
    @Path("/retrieve/{count}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveUsers(@PathParam("count") Integer count) {
        List<User> users = getUsersReservoirSampling(count);
        return Response.ok((User[]) users.toArray(new User[users.size()])).build();
    }

    private List<User> getUsersReservoirSampling(Integer count) {
        Integer k;
        Integer i;
        int j;
        Random r = new Random();
        k = count;
        List<User> R = new ArrayList<>();
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "user");
        q.limit(1000000);
        Iterator<Vertex> it = q.vertices().iterator();
        i = k;
        while ((i > 0) && (it.hasNext())) {
            try {
                R.add(UserDao.instance.get(it.next()));
            } catch (InvalidUserId ex) {
                Logger.getLogger(TestingUserResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            i--;
        }
        i = k + 1;
        while (it.hasNext()) {
            j = r.nextInt(i);
            if (j < k) {
                R.remove(j);
                try {
                    R.add(UserDao.instance.get(it.next()));
                } catch (InvalidUserId ex) {
                    Logger.getLogger(TestingUserResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                it.next();
            }
            i++;
        }
        return R;
    }
}
