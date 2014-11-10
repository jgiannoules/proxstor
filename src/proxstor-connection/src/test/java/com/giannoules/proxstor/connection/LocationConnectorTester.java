package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.LocationType;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/* 
 * unit tests for proxstorconnector's Location-centric methods
 */

public class LocationConnectorTester {
    
    private static ProxStorConnector conn;
    private static String invalidLocId;
    private Location goodLocation;
    
    public LocationConnectorTester() {
        invalidLocId = "99999";
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector(ConnectionSettings.ConnectionString);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /*
     * tests leverage a known good location
     */
    @Before
    public void setUp() {
        Location l = new Location();
        l.setAddress("12345 Main St.");
        l.setDescription("My Favorite Burger Joint");
        l.setLatitude(30.267153);
        l.setLatitude(-97.7430608);
        l.setType(LocationType.BUSINESS);
        goodLocation = conn.addLocation(l);
        assertNotNull(goodLocation);
    }
    
    @After
    public void tearDown() {
    }

    /*
     * retrieve the goodLocation based on the LocId
     * - expect a match
     */
    @Test
    public void getLocation() {
        Location l = conn.getLocation(goodLocation.getLocId());
        assertEquals(l, goodLocation);
    }
  
    /*
     * retrieve a location with known bad locid
     * - expect null
     */
    @Test
    public void getInvalidLocation() {
        assertNull(conn.getLocation(invalidLocId));
    }
    
    /*
     * delete a valid location
     * - expect true
     */
    @Test
    public void deleteLocation() {
        assertTrue(conn.deleteLocation(goodLocation.getLocId()));
    }
    
    /*
     * delete an invalid locid
     * - expect false
     */
    @Test
    public void deleteInvalidLocation() {
        assertFalse(conn.deleteLocation(invalidLocId));
    }
    
    /*
     * update field in goodLocation and update in db
     * - expect true
     */
    @Test
    public void updateLocation() {
        goodLocation.setDescription("Vegan non-Burger Joint");
        assertTrue(conn.updateLocation(goodLocation));
    }
    
    /*
     * attempt to udpate a fresh location which isn't in the db
     * - expect false
     */
    @Test
    public void updateInvalidLocation() {
        Location l = new Location();
        l.setLocId(invalidLocId.toString());
        assertFalse(conn.updateLocation(l));        
    }
    
    /*
     * add a fresh location with no valid fields
     * - expect null
     */
    @Test
    public void addInvalidLocation() {
        Location l = new Location();
        assertNull(conn.addLocation(l));
    }
    
    /*
     * add a new location with valid fields
     * - exect not null
     * - expect returned location = sent location
     */
    @Test 
    public void addValidLocation() {
        Location l = new Location();
        l.setAddress("12345 Main St.");
        l.setDescription("My Favorite Burger Joint");
        l.setLatitude(30.267153);
        l.setLatitude(-97.7430608);
        l.setType(LocationType.BUSINESS);
        Location l2 = conn.addLocation(l);
        assertNotNull(l2.getLocId());
        assertEquals(l, l2);
    }
    
}
