package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.LocationType;
import java.util.Collection;
import java.util.Collections;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/* 
 * unit tests for proxstorconnector's Location Nearby-centric methods
 */

public class LocationNearbyConnectorTester {
    
    private static ProxStorConnector conn;
    private static Location a;
    private static Location b;
    private static Location c;
    private static Location d;
    
    private static Integer invalidLocId;
    
    public LocationNearbyConnectorTester() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector("http://localhost:8080/api");
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /*
     * create known locations and prearrange the distance between
     *
     * locationA is 10m from locationB
     * locationB is 100m from locationC
     * locationD is 1000m from locationD
     */
    @Before
    public void setUp() {
        Location l = new Location();
        l.setAddress("12345 Main St.");
        l.setDescription("My Favorite Burger Joint");
        l.setLatitude(30.267153);
        l.setLatitude(-97.7430608);
        l.setType(LocationType.BUSINESS);
        a = conn.addLocation(l);
        
        l = new Location();
        l.setAddress("45 South Park Ave.");
        l.setDescription("My Favorite Coffeehouse");
        l.setLatitude(30.267153);
        l.setLatitude(-97.7430608);
        l.setType(LocationType.BUSINESS);
        b = conn.addLocation(l);
        
        l = new Location();
        l.setAddress("99 E 21st St.");
        l.setDescription("Work");
        l.setLatitude(30.267153);
        l.setLatitude(-97.7430608);
        l.setType(LocationType.BUSINESS);
        c = conn.addLocation(l);
        
        l = new Location();
        l.setAddress("10345 Central Ct.");
        l.setDescription("The Gym");
        l.setLatitude(30.267153);
        l.setLatitude(-97.7430608);
        l.setType(LocationType.BUSINESS);
        d = conn.addLocation(l);
        
        assertTrue(conn.addLocationNearby(Integer.parseInt(a.getLocId()), Integer.parseInt(b.getLocId()), 10));
        assertTrue(conn.addLocationNearby(Integer.parseInt(b.getLocId()), Integer.parseInt(c.getLocId()), 100));
        assertTrue(conn.addLocationNearby(Integer.parseInt(c.getLocId()), Integer.parseInt(d.getLocId()), 1000));
        
        invalidLocId = Integer.parseInt(d.getLocId()) + 1;
    }
    
    @After
    public void tearDown() {
    }
    
    /*
     * add location nearby which doesn't exist
     * - expect true
     */
    @Test
    public void addNearby() {
        assertTrue(conn.addLocationNearby(Integer.parseInt(a.getLocId()), Integer.parseInt(d.getLocId()), 10000));
    }
    
    /*
     * add location nearby, but relationship already established
     * add location nearby with starting location invalid
     * add location nearby with ending location invalid
     * add location nearby with both locations invalid
     * - expect false for all
     */
    @Test
    public void addNearbyInvalid() {
        assertFalse(conn.addLocationNearby(Integer.parseInt(a.getLocId()), Integer.parseInt(b.getLocId()), 1000));
        assertFalse(conn.addLocationNearby(invalidLocId, Integer.parseInt(b.getLocId()), 1000));
        assertFalse(conn.addLocationNearby(Integer.parseInt(a.getLocId()), invalidLocId, 1000));
        assertFalse(conn.addLocationNearby(invalidLocId, invalidLocId, 1000));
    }
    
    /*
     * get all locations within 9m of location a
     * - expect empty lisy
     * get all locations within 10m of location a
     * - expect set {b}
     */
    @Test
    public void getNearby() {
        assertEquals(conn.getLocationsNearby(Integer.parseInt(a.getLocId()), 9), Collections.EMPTY_LIST);
        Collection<Location> locations = conn.getLocationsNearby(Integer.parseInt(a.getLocId()), 10);
        assertEquals(locations.size(), 1);
        assertTrue(locations.contains(b));
    }
    
    /*
     * get all locations within 1km of an invalid id
     * - expect null
     */
    @Test
    public void getNearbyInvalid() {
        assertNull(conn.getLocationsNearby(invalidLocId, 1000));        
    }
    
    /*
     * test location c within 1000m of location d
     * test location b within 100000m of location c
     * - expect true for both
     * test location b within 50m of location c
     * test location a within 9m of location b
     * - expect false for both
     */
    @Test
    public void testNearby() {
        assertTrue(conn.isLocationNearby(Integer.parseInt(c.getLocId()), Integer.parseInt(d.getLocId()), 1000));
        assertTrue(conn.isLocationNearby(Integer.parseInt(b.getLocId()), Integer.parseInt(c.getLocId()), 100000));
        assertFalse(conn.isLocationNearby(Integer.parseInt(b.getLocId()), Integer.parseInt(c.getLocId()), 50));
        assertFalse(conn.isLocationNearby(Integer.parseInt(a.getLocId()), Integer.parseInt(b.getLocId()), 9));
    }
    
    /*
     * update distance between a -> b, b -> c, c -> d
     * - expect true for all
     */
    @Test
    public void updateNearby() {
        assertTrue(conn.updateLocationNearby(Integer.parseInt(a.getLocId()), Integer.parseInt(b.getLocId()), 10));
        assertTrue(conn.updateLocationNearby(Integer.parseInt(b.getLocId()), Integer.parseInt(c.getLocId()), 1000));
        assertTrue(conn.updateLocationNearby(Integer.parseInt(c.getLocId()), Integer.parseInt(d.getLocId()), 1));        
    }
    
    /*
     * update distance between a -> c (not valid nearby), invalidid -> c, c -> invalid, invalid -> invalid
     * - expect false for all
     */
    @Test
    public void updateNearbyInvalid() {
        assertFalse(conn.updateLocationNearby(Integer.parseInt(a.getLocId()), Integer.parseInt(c.getLocId()), 10));
        assertFalse(conn.updateLocationNearby(invalidLocId, Integer.parseInt(c.getLocId()), 1000));
        assertFalse(conn.updateLocationNearby(Integer.parseInt(c.getLocId()), invalidLocId, 1));
        assertFalse(conn.updateLocationNearby(invalidLocId, invalidLocId, 900));
    }
    
    /*
     * delete nearby relationship for all those created in @Before, then do it again
     * - expect 1st delete to return true
     * - expect 2nd delete to return false
     */
    @Test
    public void deleteNearby() {
        assertFalse(conn.deleteLocationNearby(Integer.parseInt(c.getLocId()), Integer.parseInt(a.getLocId())));
        assertTrue(conn.deleteLocationNearby(Integer.parseInt(a.getLocId()), Integer.parseInt(b.getLocId())));        
        assertFalse(conn.deleteLocationNearby(Integer.parseInt(a.getLocId()), Integer.parseInt(b.getLocId())));
        assertTrue(conn.deleteLocationNearby(Integer.parseInt(b.getLocId()), Integer.parseInt(c.getLocId())));
        assertFalse(conn.deleteLocationNearby(Integer.parseInt(b.getLocId()), Integer.parseInt(c.getLocId())));
        assertTrue(conn.deleteLocationNearby(Integer.parseInt(c.getLocId()), Integer.parseInt(d.getLocId())));
        assertFalse(conn.deleteLocationNearby(Integer.parseInt(c.getLocId()), Integer.parseInt(d.getLocId())));
    }
    
    /*
     * delete nearby from a -> invalid, invalid -> b, invalid -> invalid
     * - expect false for all
     */
    @Test
    public void deleteNearbyInvalid() {
        assertFalse(conn.deleteLocationNearby(Integer.parseInt(a.getLocId()), invalidLocId));
        assertFalse(conn.deleteLocationNearby(invalidLocId, Integer.parseInt(b.getLocId())));        
        assertFalse(conn.deleteLocationNearby(invalidLocId, invalidLocId));
    }
}
