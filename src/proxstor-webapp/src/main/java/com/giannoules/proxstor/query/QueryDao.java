package com.giannoules.proxstor.query;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Query;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.checkin.CheckinDao;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.knows.KnowsDao;
import com.giannoules.proxstor.locality.LocalityDao;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.nearby.NearbyDao;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Direction.OUT;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public enum QueryDao {

    instance;

    private QueryDao() {
    }

    public Collection<Locality> getMatching(Query q) throws InvalidUserId, InvalidLocationId {
        ProxStorDebug.println("getMatching");
        String userId;
        String locId = null;
        Integer strength = null;
        Double distance = null;
        Date dateStart = null;
        Date dateEnd = null;        

        /*
         * all ProxStor queries must include valid userId
         * other fields are optional and vary the style of query
         */
        UserDao.instance.validOrException(q.getUserId());

        /*
         * set local variables appropriately
         */
        userId = q.getUserId();
        if (q.getLocationId() != null) {
            LocationDao.instance.validOrException(q.getLocationId());
            locId = q.getLocationId();
        }
        if (q.getStrength() != null) {
            strength = q.getStrength();
            if ((strength < 0) || (strength > 100)) {
                return null;
            }
        }
        if (q.getDistance() != null) {
            distance = q.getDistance();
        }
        if (q.getDateStart() != null) {
            dateStart = q.getDateStart();
        }
        if (q.getDateEnd() != null) {
            dateEnd = q.getDateEnd();
        }

        /*
         * determine appropriate query type handler
         */
        if ((locId == null) && (strength == null) && (dateStart == null) && (dateEnd == null)) {
            return queryType0(userId);
        }

        if ((locId == null) && (strength == null) && (dateStart != null)) {
            return queryType1(userId, dateStart, dateEnd, distance);
        }

        if ((strength != null) && (locId == null) && (dateStart == null) && (dateEnd == null)) {
            return queryType2(userId, strength, distance);
        }

        if ((strength != null) && (dateStart != null) && (locId == null)) {
            return queryType3(userId, strength, dateStart, dateEnd);
        }

        if ((locId != null) && (strength != null) && (dateStart == null) && (dateEnd == null)) {
            return queryType4(userId, strength, distance, locId);
        }

        if ((locId != null) && (strength != null) && (dateStart != null)) {
            return queryType5(userId, strength, locId, dateStart, dateEnd);
        }

        return null;
    }
  
    /*
     * type 0 - only userId specified.
     * action - return userId's current active Locality (if any)
     */
    private Collection<Locality> queryType0(String userId) throws InvalidUserId {
        ProxStorDebug.println("queryType0");
        Collection<Locality> localities = new ArrayList<>();
        Locality l = CheckinDao.instance.getCurrentLocality(userId);
        if (l != null) {
            localities.add(l);
        }
        return localities;
    }
    
    /*
     * type 1 - userId and dateStart specified. dateEnd is optional. others null.
     * action - return userId's previous localities within date range (up to 1024)
     *          with optional distance constraint
     */
    private Collection<Locality> queryType1(String userId, Date dateStart, Date dateEnd, Double distance) throws InvalidUserId {
        ProxStorDebug.println("queryType1");
        if (dateEnd == null) {      // if no dateEnd then assume end is current date
            dateEnd = new Date();
        }        
        return CheckinDao.instance.getPreviousLocalitiesDateRange(userId, dateStart, dateEnd, 1024); // NOTE: 1024 limit
    }

    /*
     * type 2 - userId and strength specified. others null
     * action - return matching friends' current locality (up to 1024)
     *          optionally specifying distance restricts results to 
     *          distance from submitters current position
     */
    private Collection<Locality> queryType2(String userId, Integer strength, Double distance) throws InvalidUserId {
        ProxStorDebug.println("queryType2");
        Collection<Locality> localities = new ArrayList<>();
        Collection<User> users;
        Location loc = null;
        Locality l;
        try {
            Locality userLocality = CheckinDao.instance.getCurrentLocality(userId);
            if (userLocality != null) {
                loc = LocationDao.instance.get(userLocality.getLocationId());
            }
        } catch (InvalidLocationId ex) {
            loc = null;
        }
        users = KnowsDao.instance.getUserKnows(userId, strength, OUT, 1024); // NOTE: max 1024 users returned
        for (User u : users) {
            ProxStorDebug.println(u.toString());
            l = CheckinDao.instance.getCurrentLocality(u.getUserId());
            if (l != null) {
                if (loc == null) {
                    localities.add(l);
                } else {
                    try {
                        Location friendLoc = LocationDao.instance.get(l.getLocationId());
                        if (NearbyDao.instance.distanceBetweenLocations(loc, friendLoc) <= distance) {
                            localities.add(l);
                        }
                    } catch (InvalidLocationId ex) {
                    }
                }
            }

        }
        return localities;
    }

    /*
     * type 3 - userId, strength, and dateStart specified. dateEnd optional. others null
     * action - return matching friends' localities in date range (1048576 max)
     */
    private Collection<Locality> queryType3(String userId, Integer strength, Date dateStart, Date dateEnd) throws InvalidUserId {
        ProxStorDebug.println("queryType3");
        Collection<Locality> localities = new ArrayList<>();
        if (dateEnd == null) {      // if no dateEnd then assume NOW
            dateEnd = new Date();
        }
        Collection<User> friends;
        friends = KnowsDao.instance.getUserKnows(userId, strength, OUT, 1024); // NOTE: max 1024 users returned
        for (User u : friends) {
            List<Locality> friendLocalities;
            friendLocalities = CheckinDao.instance.getPreviousLocalitiesDateRange(u.getUserId(), dateStart, dateEnd, 1024); // NOTE: 1024 max
            if ((friendLocalities != null) && (!friendLocalities.isEmpty())) {
                for (Locality l : friendLocalities) {
                    localities.add(l);
                }
            }
        }
        return localities;
    }

    /*
     * type 4 - userId, locId, and strength specified. others null
     * action - return matching friends currently in specified location
     *          optionally specifying distance restricts results to 
     *          distance from submitters chosen position
     */
    private Collection<Locality> queryType4(String userId, Integer strength, Double distance, String locId) throws InvalidUserId {
        ProxStorDebug.println("queryType4");
        Collection<Locality> candidates = queryType2(userId, strength, distance);
        Collection<Locality> localities = new ArrayList<>();
        for (Locality l : candidates) {
            if (l.getLocationId().equals(locId)) {
                localities.add(l);
            }
        }
        return localities;
    }

    /*
     * type 5 - userId, locId, strength, and dateStart specified. dateEnd optional
     * action - return all those matching friends who were in location in date range
     */
    private Collection<Locality> queryType5(String userId, Integer strength, String locId, Date dateStart, Date dateEnd) throws InvalidUserId {
        ProxStorDebug.println("queryType5");
        Collection<Locality> localities = new ArrayList<>();
        if (dateEnd == null) {      // if no dateEnd then assume NOW
            dateEnd = new Date();
        }
        Collection<User> friends;
        friends = KnowsDao.instance.getUserKnows(userId, strength, OUT, 1024); // NOTE: max 1024 users returned
        for (User u : friends) {
            List<Locality> friendLocalities;
            friendLocalities = CheckinDao.instance.getPreviousLocalitiesDateRangeLocation(u.getUserId(), dateStart, dateEnd, locId, 1024); // NOTE: 1024 max
            if ((friendLocalities != null) && (!friendLocalities.isEmpty())) {
                for (Locality l : friendLocalities) {
                    localities.add(l);
                }
            }
        }
        return localities;
    }
}
