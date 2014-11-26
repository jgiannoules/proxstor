package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Query;
import com.giannoules.proxstor.api.Environmental;
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
 * <li>Environmental
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
                                Environmental.class,
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
    public User getUser(String userId) {
        String path = cleanPath("user/" + userId);
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
        String path = cleanPath("user/" + u.getUserId());
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
    public boolean deleteUser(String userId) {
        String path = cleanPath("user/" + userId);
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
    public Collection<User> getKnows(String userId, Integer strength) {
        String path = cleanPath("user/" + userId + "/knows/strength/" + strength);
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
    public Collection<User> getKnowsReverse(String userId, Integer strength) {
        String path = cleanPath("user/" + userId + "/knows/strength/" 
                + strength + "/reverse");
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
     * establish a user knows relationship between two users. the strength of
     * the relationship must be specified.
     * 
     * User u --- knows {strength: strength} ---> User v
     * 
     * @param u the user who knows the other user
     * @param v the user who is known by the other user
     * @param strength the strength of the knows relationship. valid values [1..100].
     * @return true if the relationship is established; false otherwise
     */
    public boolean addUserKnows(User u, User v, int strength) {        
        String path = cleanPath("user/" + u.getUserId() + "/knows/strength/" + strength
                + "/user/" + v.getUserId()); 
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .post(null);
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * update the knows relationship strength between users. a previous knows
     * must have been established from User u to User v.
     * 
     * @param u the user who knows the other user
     * @param v the user who is known by the other user
     * @param strength the new strength the knows relationship. valid values [1..100].
     * @return true if the relationship is updated; false otherwise
     */
    public boolean updateUserKnows(User u, User v, int strength) {
        String path = cleanPath("user/" + u.getUserId() + "/knows/strength/" + strength 
                + "/user/" + v.getUserId());
        Response response = target.path(path)
                .request()
                .put((Entity.entity("", MediaType.TEXT_PLAIN_TYPE)));        
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    /**
     * remove the knows relationship strength between users. a previous knows
     * must have been established from User u to User v.
     * 
     * @param u the user who knows the other user
     * @param v the user who is known by the other user
     * @return true if the relationship is removed; false otherwise
     */
    public boolean deleteKnows(User u, User v) {
        String path = cleanPath("user/" + u.getUserId() + "/knows/user/" 
                + v.getUserId());
        Response response = target.path(path)
                .request(MediaType.TEXT_PLAIN)
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    // END user knows actions
    
    /**
     * add a new location to the database
     * 
     * @param l the new location to add
     * @return Location instance with correct location id (locId) reference for
     * the running database behind proxstor.
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
     * retrieve location based on location id (locId)
     * 
     * @param locId the location id corresponding to a valid location within proxstor
     * @return location associated with locId; null otherwise
     */
    public Location getLocation(String locId) {
        String path = cleanPath("location/" + locId);
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Location.class);
        }
        return null;
    }

    /**
     * update an existing location within proxstor. any field of the location
     * object may be modified except the location id (locId)
     * 
     * @param l updated location object with locId set to the location object which to update
     * @return true if updated; false otherwise
     */
    public boolean updateLocation(Location l) {
        String path = cleanPath("location/" + l.getLocId());
        Response response = target.path(path)
                .request()
                .put(Entity.entity(l, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * remove an existing location from proxstor.
     * 
     * @param locId id of location to remove
     * @return true if location removed; false otherwise
     */
    public boolean deleteLocation(String locId) {
        String path = cleanPath("location/" + locId);
        Response response = target.path(path)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * retrieve a specific device associated with a user.
     * 
     * note that all devices are associated with a user. devices cannot be
     * retrieved in a vacuum without knowing the associated user.
     * 
     * @param userId user owning the device
     * @param devId device to retrieve
     * @return Device object if userId and deviceId are valid; null otherwise
     */
    public Device getDevice(String userId, String devId) {
        String path = cleanPath("/user/" + userId + "/device/" + devId);
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Device.class);
        }
        return null;
    }

    /**
     * retrieve all devices used by a user.
     * 
     * @param userId id of the user whose devices to retrieve
     * @return list of devices associated with user, including possibly an 
     * empty list if the user has no devices; null if userId is invalid.
     */
    public Collection<Device> getDevices(String userId) {
        String path = cleanPath("/user/" + userId + "/device");
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
     * add a new device to the database and associate it with a user. the user
     * already exists in the database. the device will be newly added.
     * 
     * @param userId id of user who uses the new device.
     * @param dev new device to add
     * @return Device object with correct devId if successfully added; null otherwise
     */
    public Device addDevice(String userId, Device dev) {
        String path = cleanPath("/user/" + userId + "/device/");
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(dev, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Device.class);
        }
        return null;
    }

    /**
     * update device associated with user.
     * 
     * the userId user must already use the device d.devId
     * 
     * @param userId user who uses the device
     * @param d device to update. note that d.devId must be correct
     * @return true if successful; false otherwise
     */
    public boolean updateDevice(String userId, Device d) {
        String path = cleanPath("/user/" + userId + "/device/" + d.getDevId());
        Response response = target.path(path)
                .request()
                .put(Entity.entity(d, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * delete device used by user.
     * 
     * @param userId user who uses device
     * @param devId device id to remove
     * @return true if removal is successful; false otherwise
     */
    public boolean deleteDevice(String userId, String devId) {
        String path = cleanPath("/user/" + userId + "/device/" + devId);
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
    public Environmental addEnvironmental(String locId, Environmental s) {
        String path = cleanPath("/location/" + locId + "/environmental/");
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(s, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Environmental.class);
        }
        return null;
    }

    /**
     * 
     * @param locId
     * @return 
     */
    public Collection<Environmental> getEnvironmentals(String locId) {
        String path = cleanPath("/location/" + locId + "/environmental");
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<Environmental>>() {
            }.getType();
            Collection<Environmental> environmentals = gson.fromJson(json, collectionType);
            return environmentals;
        }
        return null;
    }
    
    /**
     * 
     * @param locId
     * @param e
     * @return 
     */
    public boolean updateEnvironmental(String locId, Environmental e) {
        String path = cleanPath("/location/" + locId + "/environmental/" + e.getEnvironmentalId());
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(e, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    /**
     * 
     * @param locId
     * @param environmentalId
     * @param envivornmentalId
     * @return 
     */
    public boolean deleteEnvironmental(String locId, String environmentalId) {
        String path = cleanPath("/location/" + locId + "/environmental/" + environmentalId);
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
    public Collection<Location> getLocationsWithin(String locId) {
        String path = cleanPath("/location/" + locId + "/within");
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
    public Collection<Location> getLocationsWithinReverse(String locId) {
        String path = cleanPath("/location/" + locId + "/within/reverse");
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
    public boolean addLocationWithin(String locIdA, String locIdB) {
        String path = cleanPath("location/" + locIdA + "/within/location/" + locIdB);
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
    public boolean isLocationWithin(String locIdA, String locIdB) {
        String path = cleanPath("/location/" + locIdA + "/within/location/" + locIdB);
        Response response = target.path(path).request().get();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param locIdA
     * @param locIdB
     * @return 
     */
    public boolean deleteLocationWithin(String locIdA, String locIdB) {
       String path = cleanPath("/location/" + locIdA + "/within/location/" + locIdB);
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
    public boolean addLocationNearby(String locIdA, String locIdB, int d) {
        String path = cleanPath("location/" + locIdA + "/nearby/distance/" + d 
                + "/location/" + locIdB);
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
    public boolean isLocationNearby(String locId, String locId2, Integer distance) {
        String path = cleanPath("/location/" + locId + "/nearby/distance/" + distance 
                + "/location/" + locId2);
        Response response = target.path(path).request().get();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param locId
     * @param distance
     * @return 
     */
    public Collection<Location> getLocationsNearby(String locId, long distance) {
        String path = cleanPath("/location/" + locId + "/nearby/distance/" + distance);
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
    public boolean updateLocationNearby(String locId, String locId2, long distance) {
        String path = cleanPath("/location/" + locId + "/nearby/distance/" + distance 
                + "/location/" + locId2);
        Response response = target.path(path).request().put((Entity.entity("", MediaType.TEXT_PLAIN_TYPE)));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }

    /**
     * 
     * @param locId
     * @param locId2
     * @return 
     */
    public boolean deleteLocationNearby(String locId, String locId2) {
        String path = cleanPath("/location/" + locId 
                + "/nearby/distance/0/location/" + locId2);
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
    public Locality getLocality(String localityId) {
        String path = cleanPath("locality/" + localityId);
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
        String path = cleanPath("locality/" + l.getLocalityId());
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
    public boolean deleteLocality(String localityId) {
        String path = cleanPath("locality/" + localityId);
        Response response = target.path(path)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    /**
     * check into location when user manually specifies location
     * @param userId user id checking in
     * @param locId location id being checked into
     * @return new Locality object if checkin successful; null otherwise
     */
    public Locality userCheckinLocation(String userId, String locId) {
        String path = cleanPath("/checkin/user/" + userId + "/location/" + locId);
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(null);
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Locality.class);
        }
        return null;
    }
    
    /**
     * check out of location when user manually reports no longer being in
     * a location
     * 
     * @param userId user id checking out
     * @param locId location id the user has left
     * @return true if checkout successful; false otherwise
     */
    public boolean userCheckoutLocation(String userId, String locId) {
        String path = cleanPath("/checkin/user/" + userId + "/location/" + locId);
        Response response = target.path(path)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }     
     
    /**
     * check into location when a device detects an environmental
     * 
     * @param devId device id performing the detection
     * @param environmentalId the id of the environmental being detected
     * @return new Locality object if checkin successful; null otherwise
     */
    public Locality deviceDetectsEnvironmentalId(String devId, String environmentalId) {
        String path = cleanPath("/checkin/device/" + devId + "/environmental/" + environmentalId);
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(null);
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Locality.class);
        }
        return null;
    }
    
    /**
     * check out of location when a device no longer detects an environmental
     * 
     * @param devId device id of device no longer detecting
     * @param environmentalId the id of the environmental no longer detected
     * @return true if checkout successful; false otherwise
     */
    public boolean deviceUndetectsEnvironmentalId(String devId, String environmentalId) {
        String path = cleanPath("/checkin/device/" + devId + "/environmental/" + environmentalId);
        Response response = target.path(path)
                .request()
                .delete();
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    /**
     * check into location when device detects environmental fingerprint
     * 
     * @param devId device id performing the detection
     * @param e partially specified Environmental representing what the device
     * currently is detecting
     * @return new Locality object if checkin successful; null otherwise
     */
    public Locality deviceDetectsEnvironmental(String devId, Environmental e) {
        String path = cleanPath("/checkin/device/" + devId + "/environmental/");
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(e, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            return response.readEntity(Locality.class);
        }
        return null;
    }
    
    /**
     * check out of location, when a device no longer detects an environmental
     * 
     * @param devId device id performing the detection
     * @param s envorinmental detected
     * @return true if checkout successful; false otherwise
     */
    public boolean deviceUndetectsEnvironmental(String devId, Environmental s) {
        String path = cleanPath("/checkin/device/" + devId + "/environmental/");
        Response response = target.path(path)
                .request()
                .method("DELETE", Entity.entity(s, MediaType.APPLICATION_JSON_TYPE));
        return response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL;
    }
    
    /**
     * submit Query to proxstor
     * 
     * @param q Query to submit
     * @return results of query, as list of Locality
     */
    public Collection<Locality> query(Query q) {
        String path = "/query";
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(q, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<Locality>>() {
            }.getType();
            Collection<Locality> localities = null;// gson.fromJson(json, collectionType);
            return localities;
        }
        return null;
    }
    
    /**
     * @TODO
     * not implemented
     */
    public Collection<Locality> queryComplex() {
        return null;
    }
    
    /**
     * replace all non-standard characters in path/URL in the appropriate
     * percent encoding
     * 
     * currently supports
     *  # -> %23
     *  : -> %3A
     * 
     * @param path path to encode
     * @return encoded path
     */
    private String cleanPath(String path) {
        String s;
        s = path.replaceAll("#", "%23");
        s = s.replaceAll(":", "%3A");
        return s;   
    }
    
    /**
     * retrieve count random devices from proxstor
     * 
     * for testing and evaluation purposes only.
     *  
     * @param count number of devices to retrieve
     * @return list of devices, count in length
     */
    public Collection<Device> getTestingRandomDevices(Integer count) {
        String path = cleanPath("/admin/testing/devices/retrieve/" + count);
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
     * retrieve count random users from proxstor
     * 
     * for testing and evaluation purposes only.
     *  
     * @param count number of users to retrieve
     * @return list of users, count in length
     */
    public Collection<User> getTestingRandomUsers(Integer count) {
        String path = cleanPath("/admin/testing/users/retrieve/" + count);
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
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
     * retrieve count random locations from proxstor
     * 
     * for testing and evaluation purposes only.
     *  
     * @param count number of locations to retrieve
     * @return list of locations, count in length
     */
    public Collection<Location> getTestingRandomLocations(Integer count) {
        String path = cleanPath("/admin/testing/locations/retrieve/" + count);
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
     * retrieve count random environmentals from proxstor
     * 
     * for testing and evaluation purposes only.
     *  
     * @param count number of environmentals to retrieve
     * @return list of environmentals, count in length
     */
    public Collection<Environmental> getTestingRandomEnvironmentals(Integer count) {
        String path = cleanPath("/admin/testing/environmentals/retrieve/" + count);
        Response response = target.path(path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
            String json = response.readEntity(String.class);
            Type collectionType = new TypeToken<Collection<Environmental>>() {
            }.getType();
            Collection<Environmental> environmentals = gson.fromJson(json, collectionType);

            return environmentals;
        }
        return null;
    }

}


