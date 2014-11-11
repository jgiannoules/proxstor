package com.giannoules.proxstor.admin.testing;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/testing")
public class TestingResource {

    static Integer NUM_THREADS = 16;

    @Path("/users/{count}")
    @POST
    public Response genUsers(@PathParam("count") String s) {
        Integer count = Integer.parseInt(s);
        UserGenerator ug = new UserGenerator(new Random());
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            Runnable worker = new UserWorker(ug, count / NUM_THREADS);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return Response.noContent().build();
    }

    @Path("/locations/{count}")
    @POST
    public Response genLocations(@PathParam("count") String s) {
        Integer count = Integer.parseInt(s);
        LocationGenerator lg = new LocationGenerator(new Random());
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            Runnable worker = new LocationWorker(lg, count / NUM_THREADS);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return Response.noContent().build();
    }
}
