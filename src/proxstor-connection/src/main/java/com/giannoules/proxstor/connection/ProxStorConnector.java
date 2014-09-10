/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.api.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class ProxStorConnector {

    WebTarget target;
    Gson gson;

    public ProxStorConnector(String path) {
        final Map<String, Object> properties = new HashMap<>();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ResourceConfig config = new ResourceConfig()
                .register(new MoxyXmlFeature(
                                properties,
                                classLoader,
                                true,
                                User.class));
        target = ClientBuilder.newClient().target(path);
        gson = new Gson();
    }

    /*
     * User actions
     *
     *   URI            Method	Header                          Description
     *   --------------------------------------------------------------------------------------------------
     *   /              POST	Content-Type: application/json	add new user; new userid in location header
     *   /{userid}	GET	Accept: application/json	retrieve userid
     *   /{userid}	PUT	Content-Type: application/json	update userid user
     *   /{userid}	DELETE	n/a                             delete userid user
     */
    public User addUser(User u) {
        return target.path("/users")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(u, MediaType.APPLICATION_JSON_TYPE), User.class);
    }

    public User getUser(Integer userId) {
        return target.path("users/" + userId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(User.class);
    }

    public boolean updateUser(User u) {
        Response response = target.path("users/" + u.getUserId())
                .request()
                .put(Entity.entity(u, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public boolean deleteUser(User u) {
        Response response = target.path("users/" + u.getUserId())
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    
    /*
     * User Knows actions
     *
     * All User Knows actions relative to the URI:
     *
     *  /proxstor-webapp/api/users/{userid}/knows
     *
     *  URI             Method	Header                      Description
     *  ----------------------------------------------------------------------------------------------------------------
     *  /{s}            GET	Accept: application/json    get users userid knows with min strength s (0 for all)
     *  /{s}/{user2}	POST	Accept: application/json    establish knows relationship with strength s from userid to user2
     *  /{s}/{user2}    PUT	Accept: application/json    update knows relationship with strength s from userid to user2
     *  /{s}/{user2}	DELETE	Accept: application/json    delete knows relationshipfrom userid to user2; strength ignored
     *  /{s}/reverse	GET	Accept: application/json    get users who know userid with min strength s (0 for all)
     *
     */
    
    public Collection<User> getKnows(Integer userId, Integer strength) {
        String json = target.path("users/" + userId + "/knows" + "/" + strength)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        Type collectionType = new TypeToken<Collection<User>>() {
        }.getType();
        Collection<User> users = gson.fromJson(json, collectionType);

        return users;
    }

    public void addUserKnows(User u, User v, int strength) {
        String path = "users/" + u.getUserId() + "/knows/" + strength + "/" + v.getUserId();
        target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
    }

    public void updateUserKnows(User u, User v, int strength) {
        String path = "users/" + u.getUserId() + "/knows/" + strength + "/" + v.getUserId();
        target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
    }
    
    
    public Collection<User> searchUsers(User search) {
        String json = target.path("search/users")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(search, MediaType.APPLICATION_JSON_TYPE), String.class);

        Type collectionType = new TypeToken<Collection<User>>() {
        }.getType();
        Collection<User> users = gson.fromJson(json, collectionType);

        return users;
    }

    public Location putLocation(Location l) {
        return target.path("/locations")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(l, MediaType.APPLICATION_JSON_TYPE), Location.class);
    }

    public Location getLocation(Integer locId) {
        return target.path("locations/" + locId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Location.class);
    }



    public Device getDevice(Integer userId, Integer devId) {
        return target.path("/users/" + userId + "/devices/" + devId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Device.class);
    }

    public Collection<Device> getDevices(Integer userId) {
        String json = target.path("/users/" + userId + "/devices")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        Type collectionType = new TypeToken<Collection<Device>>() {
        }.getType();
        Collection<Device> devices = gson.fromJson(json, collectionType);

        return devices;
    }

    public Device putDevice(Integer userId, Device dev) throws Exception {
        try {
            return target.path("/users/" + userId + "/devices/")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(dev, MediaType.APPLICATION_JSON_TYPE), Device.class);
        } catch (javax.ws.rs.InternalServerErrorException ex) {
            throw new Exception("Cannot add " + dev + " to " + userId);
        }
    }

    public Sensor putSensor(Integer locId, Sensor s) throws Exception {
        try {
            return target.path("/locations/" + locId + "/sensors/")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(s, MediaType.APPLICATION_JSON_TYPE), Sensor.class);
        } catch (javax.ws.rs.InternalServerErrorException ex) {
            throw new Exception("Cannot add " + s + " to " + locId);
        }
    }

    public Collection<Sensor> getSensors(Integer locId) {
        String json = target.path("/locations/" + locId + "/sensors")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        Type collectionType = new TypeToken<Collection<Sensor>>() {
        }.getType();
        Collection<Sensor> sensors = gson.fromJson(json, collectionType);

        return sensors;
    }

    public Collection<Location> getNearby(Integer locId, long distance) {
        String json = target.path("/locations/" + locId + "/nearby/" + distance)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        Type collectionType = new TypeToken<Collection<Location>>() {
        }.getType();
        Collection<Location> locations = gson.fromJson(json, collectionType);

        return locations;
    }

    public Collection<Location> getWithin(Integer locId) {
        String json = target.path("/locations/" + locId + "/within")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        Type collectionType = new TypeToken<Collection<Location>>() {
        }.getType();
        Collection<Location> locations = gson.fromJson(json, collectionType);

        return locations;
    }

    public Collection<Location> getWithinReverse(Integer locId) {
        String json = target.path("/locations/" + locId + "/within/reverse")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        Type collectionType = new TypeToken<Collection<Location>>() {
        }.getType();
        Collection<Location> locations = gson.fromJson(json, collectionType);

        return locations;
    }

    public void locationWithin(Location locA, Location locB) {
        String path = "locations/" + locA.getLocId() + "/within/" + locB.getLocId();
        target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
    }

    public boolean isWithin(Integer locId, Integer locId2) {
        String path = "/locations/" + locId + "/within/" + locId2;
        Response response = target.path(path).request().get();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public void locationNearby(Location locA, Location locB, int d) {
        String path = "locations/" + locA.getLocId() + "/nearby/" + d + "/" + locB.getLocId();
        target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
    }

    public boolean isNearby(Integer locId, Integer locId2, Integer distance) {
        String path = "/locations/" + locId + "/nearby/" + distance + "/" + locId2;
        Response response = target.path(path).request().get();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

}
