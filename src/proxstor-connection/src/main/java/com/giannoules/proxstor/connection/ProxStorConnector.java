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
    
    public User getUser(Integer userId) {
        return target.path("users/" + userId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(User.class);
    }

    public Collection<User> searchUsers(User search) {
        String json = target.path("search/users")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(search, MediaType.APPLICATION_JSON_TYPE), String.class);
        
        Type collectionType = new TypeToken<Collection<User>>() {}.getType();
        Collection<User> users = gson.fromJson(json, collectionType);

        return users;
    }

    public User putUser(User u) {
        return target.path("/users")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(u, MediaType.APPLICATION_JSON_TYPE), User.class);
    }

    public Location putLocation(Location l) {
        return target.path("/locations")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(l, MediaType.APPLICATION_JSON_TYPE), Location.class);
    }

    public void userKnows(User u, User v, int strength) {
        String path = "users/" + u.getUserId() + "/knows/" + strength + "/" + v.getUserId();
        target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
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
        
        Type collectionType = new TypeToken<Collection<Device>>() {}.getType();
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
    
    public void locationWithin(Location locA, Location locB) {
        String path = "locations/" + locA.getLocId() + "/within/" + locB.getLocId();
        target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
    }
    
      
    public void locationNearby(Location locA, Location locB, int d) {
        String path = "locations/" + locA.getLocId() + "/nearby/" + d + "/" + locB.getLocId();
        target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
    }
}
 