package com.giannoules.proxstor.query;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Query;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.checkin.CheckinDao;
import com.giannoules.proxstor.exception.InvalidLocalityId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.knows.KnowsDao;
import com.giannoules.proxstor.locality.LocalityDao;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum QueryDao {

    instance;

    private QueryDao() {
    }

    public Collection<Locality> getMatching(Query q) throws InvalidUserId, InvalidLocationId {
        ProxStorDebug.println("getMatching");
        String userId;
        String locId = null;
        Integer strength = null;
        Date dateStart = null;
        Date dateEnd = null;
        Collection<Locality> localities = new ArrayList<>();

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
            return queryType1(userId, dateStart, dateEnd);
        }

        if ((strength != null) && (locId == null) && (dateStart == null) && (dateEnd == null)) {
            return queryType2(userId, strength);
        }

        if ((strength != null) && (dateStart != null) && (locId == null)) {
            return queryType3(userId, strength, dateStart, dateEnd);
        }

        if ((locId != null) && (strength != null) && (dateStart == null) && (dateEnd == null)) {
            return queryType4(userId, strength, locId);
        }

        if ((locId != null) && (strength != null) && (dateStart != null)) {
            return queryType5(userId, strength, locId, dateStart, dateEnd);
        }

        return null;
    }

    /**
     * private helper method to determine whether a date range overlaps with the
     * period of active time for a Locality.
     *
     * @param l Locality to check against
     * @param start Start of date range to compare
     * @param end End of date range to compare
     * @return true if date range (start, end) overlap with Locality l; false
     * otherwise.
     */
    private boolean localityWithinDates(Locality l, Date start, Date end) {
        ProxStorDebug.println("localityWithinDates");
        Date startLocality = l.getArrival();
        Date endLocality = l.getDeparture();
        return (startLocality.before(end) && start.before(endLocality));
    }

    private Locality getLocalityFirstInDateRange(String userId, Date start, Date end) {
        ProxStorDebug.println("getLocalityFirstInDateRange");
        Locality l;
        Vertex v, u;
        try {            
            u = ProxStorGraph.instance.getVertex(userId);
            v = CheckinDao.instance.getPrevioulsyAt(u);
            while (v != null) {
                ProxStorDebug.println(v.toString());
                ProxStorDebug.println("stepA");
                l = LocalityDao.instance.get(v);
                ProxStorDebug.println("stepB");
                if (localityWithinDates(l, start, end)) {
                    return l;
                }
                if (l.getArrival().before(start)) {
                    return null;
                }
                ProxStorDebug.println("stepC");
                v = CheckinDao.instance.getPrevioulsyAt(v);
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID | InvalidLocalityId ex) {
            Logger.getLogger(QueryDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private List<Locality> getPrevioulsyAtDateRange(String userId, Date start, Date end) throws InvalidUserId {
        ProxStorDebug.println("getPrevioulsyAtDateRange");
        List<Locality> localities = new ArrayList<>();
        try {
            Locality l = getLocalityFirstInDateRange(userId, start, end);
            if (l == null) {
                return null;
            }              
            Vertex v = ProxStorGraph.instance.getVertex(l.getLocalityId());            
            do {
                if (localityWithinDates(l, start, end)) {
                    localities.add(l);
                } else {
                    return localities;                    
                }
                v = CheckinDao.instance.getPrevioulsyAt(v);
                if (v != null) {
                    l = LocalityDao.instance.get(v);
                }
            } while (v != null);
            return localities;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID | InvalidLocalityId ex) {
            Logger.getLogger(QueryDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return localities;
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
     * action - return userId's previous localities within date range
     */
    private Collection<Locality> queryType1(String userId, Date dateStart, Date dateEnd) throws InvalidUserId {
        ProxStorDebug.println("queryType1");
        if (dateEnd == null) {      // if no dateEnd then assume end is current date
            dateEnd = new Date();
        }
        return getPrevioulsyAtDateRange(userId, dateStart, dateEnd);
    }

    /*
     * type 2 - userId and strength specified. others null
     * action - return matching friends' current locality
     */
    private Collection<Locality> queryType2(String userId, Integer strength) throws InvalidUserId {
        ProxStorDebug.println("queryType2");
        Collection<Locality> localities = new ArrayList<>();
        Collection<User> users;
        Locality l;
        users = KnowsDao.instance.getUserKnows(userId, strength, OUT, 1024); // NOTE: max 1024 users returned
        for (User u : users) {
            ProxStorDebug.println(u.toString());
            l = CheckinDao.instance.getCurrentLocality(u.getUserId());
            if (l != null) {
                localities.add(l);
            }
        }
        return localities;
    }

    /*
     * type 3 - userId, strength, and dateStart specified. dateEnd optional. others null
     * action - return matching friends' localities in date range
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
            friendLocalities = getPrevioulsyAtDateRange(u.getUserId(), dateStart, dateEnd);
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
     */
    private Collection<Locality> queryType4(String userId, Integer strength, String locId) throws InvalidUserId {
        ProxStorDebug.println("queryType4");
        Collection<Locality> candidates = queryType2(userId, strength);
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
        Collection<Locality> candidates = queryType3(userId, strength, dateStart, dateEnd);
        Collection<Locality> localities = new ArrayList<>();
        for (Locality l : candidates) {
            ProxStorDebug.println(l.toString());
            if (l.getLocationId().equals(locId)) {
                localities.add(l);
            }
        }
        return localities;
    }
}
