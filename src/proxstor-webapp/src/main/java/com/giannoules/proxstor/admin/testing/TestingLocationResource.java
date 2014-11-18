package com.giannoules.proxstor.admin.testing;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.user.UserDao;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
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

public class TestingLocationResource {

     static Integer NUM_THREADS = 16;

    /**
     * generate requested number of locations and associate one unique 
     * environmental with each. locations are randomly associated with a
     * 'nearby' relationship to previously created locations
     *
     * @param count
     * @return
     */
    @Path("/generate/{count}")
    @POST
    public Response generatLocations(@PathParam("count") Integer count) {
        int threadCount = NUM_THREADS;
        if (ProxStorGraph.instance.graph instanceof TinkerGraph) {
               threadCount = 1;
        } 
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Runnable worker = new LocationWorker(count / threadCount, false);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return Response.noContent().build();
    }

    /**
     * using reservoir sampling return count locations randomly selected from
     * database
     *
     * @param count
     * @return
     */
    @Path("/retrieve/{count}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveLocations(@PathParam("count") Integer count) {
        List<Location> locations = getLocationsReservoirSampling(count);
        return Response.ok((Location[]) locations.toArray(new Location[locations.size()])).build();
    }

    private List<Location> getLocationsReservoirSampling(Integer count) {
        Integer k;
        Integer i;
        int j;
        Random r = new Random();
        k = count;
        List<Location> R = new ArrayList<>();
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "location");
        q.limit(1000000);
        Iterator<Vertex> it = q.vertices().iterator();
        i = k;
        while ((i > 0) && (it.hasNext())) {
            try {
                R.add(LocationDao.instance.get(it.next()));
            } catch (InvalidLocationId ex) {
                Logger.getLogger(TestingLocationResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            i--;
        }
        i = k + 1;
        while (it.hasNext()) {
            j = r.nextInt(i);
            if (j < k) {
                R.remove(j);
                try {
                    R.add(LocationDao.instance.get(it.next()));
                } catch (InvalidLocationId ex) {
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
