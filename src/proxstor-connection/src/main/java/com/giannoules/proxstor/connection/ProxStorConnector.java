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
                                User.class,
                                Device.class,
                                Location.class,
                                Sensor.class));
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
    /*
     * returns User with userId populated
     */
    public User addUser(User u) {
        Response response = target.path("/users")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(u, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(User.class);
        }
        return null;
    }

    /*
     * retrieves and returns user based on userId
     */
    public User getUser(Integer userId) {
        Response response = target.path("users/" + userId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(User.class);
        }
        return null;
    }

    /*
     * using u.userId to update User; return of true is success
     */
    public boolean updateUser(User u) {
        Response response = target.path("users/" + u.getUserId())
                .request()
                .put(Entity.entity(u, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /*
     * delete user based on userId; return true if successful
     */
    public boolean deleteUser(Integer userId) {
        Response response = target.path("users/" + userId)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    // END user actions

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
    /*
     * return collection of users userId knows with at least strength
     */
    public Collection<User> getKnows(Integer userId, Integer strength) {
        Response response = target.path("users/" + userId + "/knows" + "/" + strength)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);

            Type collectionType = new TypeToken<Collection<User>>() {
            }.getType();
            Collection<User> users = gson.fromJson(json, collectionType);

            return users;
        }
        return null;
    }

    /*
     * return collection of users known by userId with at least strength
     */
    public Collection<User> getKnowsReverse(Integer userId, Integer strength) {
        Response response = target.path("users/" + userId + "/knows" + "/" + strength + "/reverse")
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);

            Type collectionType = new TypeToken<Collection<User>>() {
            }.getType();
            Collection<User> users = gson.fromJson(json, collectionType);

            return users;
        }
        return null;
    }

    public boolean addUserKnows(User u, User v, int strength) {
        String path = "users/" + u.getUserId() + "/knows/" + strength + "/" + v.getUserId();
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public boolean updateUserKnows(User u, User v, int strength) {
        String path = "users/" + u.getUserId() + "/knows/" + strength + "/" + v.getUserId();
        Response response = target.path(path)
                .request()
                .put((Entity.entity("", MediaType.TEXT_PLAIN_TYPE)));        
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public boolean deleteKnows(User u, User v) {
        String path = "users/" + u.getUserId() + "/knows/0/" + v.getUserId();
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    // END user knows actions
    /*
     * Location actions
     */
    public Location addLocation(Location l) {
        Response response = target.path("/locations")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(l, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Location.class);
        }
        return null;
    }

    public Location getLocation(Integer locId) {
        Response response = target.path("locations/" + locId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Location.class);
        }
        return null;
    }

    public boolean updateLocation(Location l) {
        Response response = target.path("locations/" + l.getLocId())
                .request()
                .put(Entity.entity(l, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public boolean deleteLocation(Integer locId) {
        Response response = target.path("locations/" + locId)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /*
     * Devices
     */
    public Device getDevice(Integer userId, Integer devId) {
        Response response = target.path("/users/" + userId + "/devices/" + devId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Device.class);
        }
        return null;
    }

    public Collection<Device> getDevices(Integer userId) {
        Response response = target.path("/users/" + userId + "/devices")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<Device>>() {
            }.getType();
            Collection<Device> devices = gson.fromJson(json, collectionType);

            return devices;
        }
        return null;
    }

    public Device addDevice(Integer userId, Device dev) {
        Response response = target.path("/users/" + userId + "/devices/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(dev, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Device.class);
        }
        return null;
    }

    /*
     * using d.devId to update Device; return of true is success
     */
    public boolean updateDevice(Integer userId, Device d) {
        Response response = target.path("users/" + userId + "/devices/" + d.getDevId())
                .request()
                .put(Entity.entity(d, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /*
     * delete device based on devId; return true if successful
     */
    public boolean deleteDevice(Integer userId, Integer devId) {
        Response response = target.path("users/" + userId + "/devices/" + devId)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    // end Device Section
    
    /*
     * sensor
     */
    public Sensor addSensor(Integer locId, Sensor s) {
        String path = "/locations/" + locId + "/sensors/";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(s, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Sensor.class);
        }
        return null;
    }

    public Collection<Sensor> getSensors(Integer locId) {
        String path = "/locations/" + locId + "/sensors";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<Sensor>>() {
            }.getType();
            Collection<Sensor> sensors = gson.fromJson(json, collectionType);
            return sensors;
        }
        return null;
    }
    
    public boolean updateSensor(Integer locId, Sensor s) {
        String path = "/locations/" + locId + "/sensors/" + s.getSensorId();
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(s, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    public boolean deleteSensor(Integer locId, Integer sensorId) {
        String path = "/locations/" + locId + "/sensors/" + sensorId;
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    // end Sensors
    
    /*
     * Within
     */
    public Collection<Location> getLocationsWithin(Integer locId) {
        String path = "/locations/" + locId + "/within";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<Location>>() {
            }.getType();
            Collection<Location> locations = gson.fromJson(json, collectionType);
            return locations;
        }
        return null;
    }

    public Collection<Location> getLocationsWithinReverse(Integer locId) {
        String path = "/locations/" + locId + "/within/reverse";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<Location>>() {
            }.getType();
            Collection<Location> locations = gson.fromJson(json, collectionType);
            return locations;
        }
        return null;
    }

    public boolean addLocationWithin(Integer locIdA, Integer locIdB) {
        String path = "locations/" + locIdA + "/within/" + locIdB;
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public boolean isLocationWithin(Integer locIdA, Integer locIdB) {
        String path = "/locations/" + locIdA + "/within/" + locIdB;
        Response response = target.path(path).request().get();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public boolean deleteLocationWithin(Integer locIdA, Integer locIdB) {
       String path = "/locations/" + locIdA + "/within/" + locIdB;
       Response response = target.path(path).request().delete();
       return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    // end Within
    
    /*
     * Nearby
     */
    public boolean addLocationNearby(Integer locIdA, Integer locIdB, int d) {
        String path = "locations/" + locIdA + "/nearby/" + d + "/" + locIdB;
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public boolean isLocationNearby(Integer locId, Integer locId2, Integer distance) {
        String path = "/locations/" + locId + "/nearby/" + distance + "/" + locId2;
        Response response = target.path(path).request().get();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public Collection<Location> getLocationsNearby(Integer locId, long distance) {
        String path = "/locations/" + locId + "/nearby/" + distance;
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<Location>>() {
            }.getType();
            Collection<Location> locations = gson.fromJson(json, collectionType);
            return locations;
        }
        return null;
    }

    public boolean updateLocationNearby(Integer locId, Integer locId2, long distance) {
        String path = "/locations/" + locId + "/nearby/" + distance + "/" + locId2;
        Response response = target.path(path).request().put((Entity.entity("", MediaType.TEXT_PLAIN_TYPE)));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    public boolean deleteLocationNearby(Integer locId, Integer locId2) {
        String path = "/locations/" + locId + "/nearby/" + 0 + "/" + locId2;
        Response response = target.path(path).request().delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    // end Nearby

    /*
     * Searches !
     */
    public Collection<User> searchUsers(User search) {
        Response response = target.path("search/users")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(search, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<User>>() {
            }.getType();
            Collection<User> users = gson.fromJson(json, collectionType);

            return users;
        }
        return null;
    }

}
