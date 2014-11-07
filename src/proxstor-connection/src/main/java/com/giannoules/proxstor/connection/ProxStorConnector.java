package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Query;
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

/**
 * Provides client application interface into a ProxStor instance. Capabilities
 * exposed here includes management of:
 * <ul>
 * <li>Users
 * <li>Devices
 * <li>Locations
 * <li>Sensors
 * </ul>
 * 
 * Note that instantiation of back-end Graph instance is not provided. That must
 * be done outside this connector.
 */

public class ProxStorConnector {

    WebTarget target;
    Gson gson;

    /**
     * Instantiate ProxStorConnector and initiate a connection to a specified
     * ProxStor instance. It is recommended to reuse a single ProxStorConnector
     * for resource leveraging purposes.
     * 
     * @param path URI connection string to ProxStor. Example: "http://localhost:8080/proxstor-webapp/api"
     */
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
                                Sensor.class,
                                Locality.class));
        target = ClientBuilder.newClient(config).target(path);
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
    
    /**
     * Add a new User to the ProxStor
     * 
     * @param u Instance of the User to add to the database.
     * @return If successful the added User object is reflected back to the caller,
     * but now the userId field has been filled in with the object id from the backing
     * graph relational database. If the operation was not successful null will be
     * returned.
     */
    public User addUser(User u) {
        String path = "user";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(u, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(User.class);
        }
        return null;
    }

    /**
     * Retrieve the User instance stored under the specified object ID.
     * 
     * @param userId The backing store's object ID for the desired User.
     * @return Instance of User stored under userId, if the userId specified
     * is a valid User Object ID. If the userId is not valid null is returned.
     */
    public User getUser(Integer userId) {
        String path = "user/" + userId;
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(User.class);
        }
        return null;
    }

    /**
     * using u.userId to update User; return of true is success
     * 
     * @param u
     * @return 
     */
    public boolean updateUser(User u) {
        String path = "user/" + u.getUserId();
        Response response = target.path(path)
                .request()
                .put(Entity.entity(u, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * delete user based on userId; return true if successful
     * 
     * @param userId
     * @return 
     */
    public boolean deleteUser(Integer userId) {
        String path = "user/" + userId;
        Response response = target.path(path)
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
    
    /**
     * return collection of users userId knows with at least strength
     * 
     * @param userId
     * @param strength
     * @return 
     */
    public Collection<User> getKnows(Integer userId, Integer strength) {
        String path = "user/" + userId + "/knows/strength/" + strength;
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON).get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);

            Type collectionType = new TypeToken<Collection<User>>() {
            }.getType();
            Collection<User> users = gson.fromJson(json, collectionType);

            return users;
        }
        return null;
    }

    /**
     * return collection of users known by userId with at least strength
     * 
     * @param userId
     * @param strength
     * @return 
     */
    public Collection<User> getKnowsReverse(Integer userId, Integer strength) {
        String path = "user/" + userId + "/knows/strength/" 
                + strength + "/reverse";
        Response response = target.path(path)
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

    /**
     * 
     * @param u
     * @param v
     * @param strength
     * @return 
     */
    public boolean addUserKnows(User u, User v, int strength) {        
        String path = "user/" + u.getUserId() + "/knows/strength/" + strength 
                + "/user/" + v.getUserId();
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param u
     * @param v
     * @param strength
     * @return 
     */
    public boolean updateUserKnows(User u, User v, int strength) {
        String path = "user/" + u.getUserId() + "/knows/strength/" + strength 
                + "/user/" + v.getUserId();
        Response response = target.path(path)
                .request()
                .put((Entity.entity("", MediaType.TEXT_PLAIN_TYPE)));        
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    /**
     * 
     * @param u
     * @param v
     * @return 
     */
    public boolean deleteKnows(User u, User v) {
        String path = "user/" + u.getUserId() + "/knows/strength/0/user/" 
                + v.getUserId();
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    // END user knows actions
    
    /**
     * 
     * @param l
     * @return 
     */
    public Location addLocation(Location l) {
        String path = "/location";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(l, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Location.class);
        }
        return null;
    }

    /**
     * 
     * @param locId
     * @return 
     */
    public Location getLocation(Integer locId) {
        String path = "location/" + locId;
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Location.class);
        }
        return null;
    }

    /**
     * 
     * @param l
     * @return 
     */
    public boolean updateLocation(Location l) {
        String path = "location/" + l.getLocId();
        Response response = target.path(path)
                .request()
                .put(Entity.entity(l, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param locId
     * @return 
     */
    public boolean deleteLocation(Integer locId) {
        String path = "location/" + locId;
        Response response = target.path(path)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param userId
     * @param devId
     * @return 
     */
    public Device getDevice(Integer userId, Integer devId) {
        String path = "/user/" + userId + "/device/" + devId;
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Device.class);
        }
        return null;
    }

    /**
     * 
     * @param userId
     * @return 
     */
    public Collection<Device> getDevices(Integer userId) {
        String path = "/user/" + userId + "/device";
        Response response = target.path(path)
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

    /**
     * 
     * @param userId
     * @param dev
     * @return 
     */
    public Device addDevice(Integer userId, Device dev) {
        String path = "/user/" + userId + "/device/";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(dev, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Device.class);
        }
        return null;
    }

    /**
     *  using d.devId to update Device; return of true is success
     * @param userId
     * @param d
     * @return 
     */
    public boolean updateDevice(Integer userId, Device d) {
        String path = "user/" + userId + "/device/" + d.getDevId();
        Response response = target.path(path)
                .request()
                .put(Entity.entity(d, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * delete device based on devId; return true if successful
     * @param userId
     * @param devId
     * @return 
     */
    public boolean deleteDevice(Integer userId, Integer devId) {
        String path = "user/" + userId + "/device/" + devId;
        Response response = target.path(path)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    // end Device Section
    
    /**
     * 
     * @param locId
     * @param s
     * @return 
     */
    public Sensor addSensor(Integer locId, Sensor s) {
        String path = "/location/" + locId + "/sensor/";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(s, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Sensor.class);
        }
        return null;
    }

    /**
     * 
     * @param locId
     * @return 
     */
    public Collection<Sensor> getSensors(Integer locId) {
        String path = "/location/" + locId + "/sensor";
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
    
    /**
     * 
     * @param locId
     * @param s
     * @return 
     */
    public boolean updateSensor(Integer locId, Sensor s) {
        String path = "/location/" + locId + "/sensor/" + s.getSensorId();
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(s, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    /**
     * 
     * @param locId
     * @param sensorId
     * @return 
     */
    public boolean deleteSensor(Integer locId, Integer sensorId) {
        String path = "/location/" + locId + "/sensor/" + sensorId;
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    /**
     * 
     * @param locId
     * @return 
     */
    public Collection<Location> getLocationsWithin(Integer locId) {
        String path = "/location/" + locId + "/within";
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

    /**
     * 
     * @param locId
     * @return 
     */
    public Collection<Location> getLocationsWithinReverse(Integer locId) {
        String path = "/location/" + locId + "/within/reverse";
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

    /**
     * 
     * @param locIdA
     * @param locIdB
     * @return 
     */
    public boolean addLocationWithin(Integer locIdA, Integer locIdB) {
        String path = "location/" + locIdA + "/within/location/" + locIdB;
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param locIdA
     * @param locIdB
     * @return 
     */
    public boolean isLocationWithin(Integer locIdA, Integer locIdB) {
        String path = "/location/" + locIdA + "/within/location/" + locIdB;
        Response response = target.path(path).request().get();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param locIdA
     * @param locIdB
     * @return 
     */
    public boolean deleteLocationWithin(Integer locIdA, Integer locIdB) {
       String path = "/location/" + locIdA + "/within/location/" + locIdB;
       Response response = target.path(path).request().delete();
       return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    /**
     * 
     * @param locIdA
     * @param locIdB
     * @param d
     * @return 
     */
    public boolean addLocationNearby(Integer locIdA, Integer locIdB, int d) {
        String path = "location/" + locIdA + "/nearby/distance/" + d 
                + "/location/" + locIdB;
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param locId
     * @param locId2
     * @param distance
     * @return 
     */
    public boolean isLocationNearby(Integer locId, Integer locId2, Integer distance) {
        String path = "/location/" + locId + "/nearby/distance/" + distance 
                + "/location/" + locId2;
        Response response = target.path(path).request().get();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param locId
     * @param distance
     * @return 
     */
    public Collection<Location> getLocationsNearby(Integer locId, long distance) {
        String path = "/location/" + locId + "/nearby/distance/" + distance;
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

    /**
     * 
     * @param locId
     * @param locId2
     * @param distance
     * @return 
     */
    public boolean updateLocationNearby(Integer locId, Integer locId2, long distance) {
        String path = "/location/" + locId + "/nearby/distance/" + distance 
                + "/location/" + locId2;
        Response response = target.path(path).request().put((Entity.entity("", MediaType.TEXT_PLAIN_TYPE)));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param locId
     * @param locId2
     * @return 
     */
    public boolean deleteLocationNearby(Integer locId, Integer locId2) {
        String path = "/location/" + locId 
                + "/nearby/distance/0/location/" + locId2;
        Response response = target.path(path).request().delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param search
     * @return 
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
        
     /**
     * Add a new Locality to the ProxStor
     * 
     * @param l Instance of the Locality to add to the database.
     * @return If successful the added Locality object is reflected back to the caller,
     * but now the localityId field has been filled in with the object id from the backing
     * graph relational database. If the operation was not successful null will be
     * returned.
     */
    public Locality addLocality(Locality l) {
        String path = "locality";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(l, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Locality.class);
        }
        return null;
    }

    /**
     * Retrieve the Locality instance stored under the specified object ID.
     * 
     * @param localityId The backing store's object ID for the desired Locality.
     * @return Instance of Locality stored under localityId, if the localityId specified
     * is a valid User Object ID. If the localityId is not valid null is returned.
     */
    public Locality getLocality(Integer localityId) {
        String path = "locality/" + localityId;
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Locality.class);
        }
        return null;
    }

    /**
     * using l.localityId to update Locality; return of true is success
     * 
     * @param l
     * @return 
     */
    public boolean updateLocality(Locality l) {
        String path = "locality/" + l.getLocalityId();
        Response response = target.path(path)
                .request()
                .put(Entity.entity(l, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * delete locality based on localityId; return true if successful
     * 
     * @param localityId
     * @return 
     */
    public boolean deleteLocality(Integer localityId) {
        String path = "locality/" + localityId;
        Response response = target.path(path)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    public Locality userCheckinLocation(Integer userId, Integer locId) {
        String path = "/checkin/user/" + userId + "/location/" + locId;
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(null);
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Locality.class);
        }
        return null;
    }
    
    public boolean userCheckoutLocation(Integer userId, Integer locId) {
        String path = "/checkin/user/" + userId + "/location/" + locId;
        Response response = target.path(path)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }     
     
    public Locality deviceDetectsSensorId(Integer devId, Integer sensorId) {
        String path = "/checkin/device/" + devId + "/sensor/" + sensorId;
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(null);
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Locality.class);
        }
        return null;
    }
    
    public boolean deviceUndetectsSensorId(Integer devId, Integer sensorId) {
        String path = "/checkin/device/" + devId + "/sensor/" + sensorId;
        Response response = target.path(path)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    public Locality deviceDetectsSensor(Integer devId, Sensor s) {
        String path = "/checkin/device/" + devId + "/sensor/";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(s, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Locality.class);
        }
        return null;
    }
    
    public boolean deviceUndetectsSensor(Integer devId, Sensor s) {
        String path = "/checkin/device/" + devId + "/sensor/";
        Response response = target.path(path)
                .request()
                .method("DELETE", Entity.entity(s, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    public Collection<Locality> query(Query q) {
        String path = "/query";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(q, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<Locality>>() {
            }.getType();
            Collection<Locality> localities = gson.fromJson(json, collectionType);
            return localities;
        }
        return null;
    }
    
    // @TODO
    public Collection<Locality> queryComplex() {
        return null;
    }

}
