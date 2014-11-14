package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.User;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for ProxStorConnector's User-centric methods
 * 
 * @author James Giannoules
 */
public class UserConnectorTester  {

    private static ProxStorConnector conn;
    private static String invalidId;
    private User goodUser;
    
    public UserConnectorTester() {        
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector(ConnectionSettings.ConnectionString);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /*
     * known good user for each @Test
     * invalidId calculated as last userId + 1
     */
    @Before
    public void setUp() {
        goodUser = conn.addUser(new User("John", "Smith", "jsmith@email.com"));
        assertNotNull(goodUser);
        invalidId = goodUser.getUserId() + 1;
    }
    
    @After
    public void tearDown() {
    }

    /*
     * retrieve good user
     * - expect retrieved user equals goodUser
     */
    @Test
    public void getUser() {
        User u = conn.getUser(goodUser.getUserId());
        assertEquals(u, goodUser);
    }
    
    /*
     * attempt to retrieve invalid user id
     * - expect null
     */
    @Test
    public void getInvalidUser() {
        assertNull(conn.getUser(invalidId));
    }
    
    /*
     * delete goodUser
     * - expect true
     */
    @Test
    public void deleteUser() {
        assertTrue(conn.deleteUser(goodUser.getUserId()));
    }
    
    /*
     * delete invalid user id
     * - expect false
     */
    @Test
    public void deleteInvalidUser() {
        assertFalse(conn.deleteUser(invalidId));
    }
    
    /*
     * change a field in goodUser and update
     * - expect true
     */
    @Test
    public void updateUser() {
        goodUser.setEmail("newemail@gmail.com");
        assertTrue(conn.updateUser(goodUser));
    }
    
    /*
     * attempt to update an invalid user id
     * attempt to udpate user with valid id, but no other data
     * - expect false
     */
    @Test
    public void updateInvalidUser() {
        User u = new User();
        u.setUserId(invalidId.toString());
        assertFalse(conn.updateUser(u));
        u.setUserId(goodUser.getUserId());
        assertFalse(conn.updateUser(u));
    }

    /*
     * attempt to add a User without necessary data     
     * - expect false
     */
    @Test
    public void addInvalidUser() {
        User u = new User();
        assertNull(conn.addUser(u));
    }
    
    /*
     * attempt to add a valid user
     * - expect userid to be populated
     * - expect user mirrored back properly
     */
    @Test 
    public void addValidUser() {
        User u = conn.addUser(new User("first", "last", "somebody@email.com"));
        assertNotNull(u.getUserId());
        assertEquals(u, new User("first", "last", "somebody@email.com"));        
    }    
  
}
