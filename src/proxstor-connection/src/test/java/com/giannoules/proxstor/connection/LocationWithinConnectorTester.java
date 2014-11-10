package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.LocationType;
import java.util.Collection;
import java.util.Collections;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/* 
 * unit tests for proxstorconnector's Location Within-centric methods
 */

public class LocationWithinConnectorTester {

    private static ProxStorConnector conn;
    private static Location a;
    private static Location b;
    private static Location c;
    private static Location d;
    
    private static String invalidLocId;
    
    public LocationWithinConnectorTester() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector(ConnectionSettings.ConnectionString);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /*
     * create four locations (a, b, c, d) and establish within relationship
     *
     * a within b
     * c within d
     * b within d
     *
     * a --> b --> d <-- c
     *
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
     
        assertTrue(conn.addLocationWithin(a.getLocId(), b.getLocId()));
        assertTrue(conn.addLocationWithin(c.getLocId(), d.getLocId()));
        assertTrue(conn.addLocationWithin(b.getLocId(), d.getLocId()));      
        
        invalidLocId = d.getLocId() + 1;
    }
    
    @After
    public void tearDown() {
    }
    
    /*
     * add c --within--> a
     * - expect true
     */
    @Test
    public void addWithin() {
        assertTrue(conn.addLocationWithin(c.getLocId(), a.getLocId()));
    }
    
    /*
     * add a --within--> b (already exists)
     * add invalid --within--> invalid
     * - expect false for both
     */
    @Test
    public void addWithinInvalid() {
        assertFalse(conn.addLocationWithin(a.getLocId(), b.getLocId()));
        assertFalse(conn.addLocationWithin(invalidLocId, invalidLocId));       
    }
    
    /*
     * get all locations within d
     * - expect empty list
     * get all locations wtihin b
     * - expect {d}
     */
    @Test
    public void getWithin() {
        assertEquals(conn.getLocationsWithin(d.getLocId()), Collections.EMPTY_LIST);
        Collection<Location> locations = conn.getLocationsWithin(b.getLocId());
        assertEquals(locations.size(), 1);
        assertTrue(locations.contains(d));
    }
    
    /*
     * get all locations within invalidlocId
     * - expect null
     */
    @Test
    public void getWithinInvalid() {
        assertNull(conn.getLocationsWithin(invalidLocId)); 
    }
    
    /*
     * get locations which a is within
     * - expect []
     * get locations which d is within
     * - expect {b, c}
     */
    @Test
    public void getWithinReverse() {
        assertEquals(conn.getLocationsWithinReverse(a.getLocId()), Collections.EMPTY_LIST);
        Collection<Location> locations = conn.getLocationsWithinReverse(d.getLocId());
        assertEquals(locations.size(), 2);
        assertTrue(locations.contains(c));
        assertTrue(locations.contains(b));
    }
    
    /*
     * get locations which invalidid is within
     * - expect null
     */
    @Test
    public void getWithinReverseInvalid() {
        assertNull(conn.getLocationsWithinReverse(invalidLocId)); 
    }
    
    /*
     * test that c is within d
     * test that b is within d
     * - expect true for both
     * test that a is within a
     * test that c is within b
     * - expect false for both
     */
    @Test
    public void testWithin() {
        assertTrue(conn.isLocationWithin(c.getLocId(), d.getLocId()));
        assertTrue(conn.isLocationWithin(b.getLocId(), d.getLocId()));
        assertFalse(conn.isLocationWithin(a.getLocId(), a.getLocId()));
        assertFalse(conn.isLocationWithin(c.getLocId(), b.getLocId()));
    }
    
    /*
     * test a within invalid
     * test invalid within b
     * test invalid within invalid
     * - expect false for all
     */
    @Test
    public void testWithinInvalid() {
        assertFalse(conn.isLocationWithin(a.getLocId(), invalidLocId));
        assertFalse(conn.isLocationWithin(invalidLocId, b.getLocId()));        
        assertFalse(conn.isLocationWithin(invalidLocId, invalidLocId));
    }    
    
    /*
     * delete two times:
     *  - c within a
     *  - a within b
     *  - b within d
     *  - c wihtin d
     * - expect 1st delete returns true
     * - expect 2nd delete returns false
     */
    @Test
    public void deleteWithin() {
        assertFalse(conn.deleteLocationWithin(c.getLocId(), a.getLocId()));
        assertTrue(conn.deleteLocationWithin(a.getLocId(), b.getLocId()));        
        assertFalse(conn.deleteLocationWithin(a.getLocId(), b.getLocId()));
        assertTrue(conn.deleteLocationWithin(b.getLocId(), d.getLocId()));
        assertFalse(conn.deleteLocationWithin(b.getLocId(), d.getLocId()));
        assertTrue(conn.deleteLocationWithin(c.getLocId(), d.getLocId()));
        assertFalse(conn.deleteLocationWithin(c.getLocId(), d.getLocId()));
    }
    
    /*
     * delete a within invalid, invalid within b, invalid within invalid
     * - expect false for all
     */
    @Test
    public void deleteWithinInvalid() {
        assertFalse(conn.deleteLocationWithin(a.getLocId(), invalidLocId));
        assertFalse(conn.deleteLocationWithin(invalidLocId, b.getLocId()));        
        assertFalse(conn.deleteLocationWithin(invalidLocId, invalidLocId));
    }
}
