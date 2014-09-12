package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/* 
 * unit tests for proxstorconnector's User Knows-centric methods
 */
public class UserKnowsConnectorTester {
    
    private static ProxStorConnector conn;
    private static User a;
    private static User b;
    private static User c;
    private static User d;
    private static Integer invalidUserId;
    
    public UserKnowsConnectorTester() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector("http://localhost:8080/api");       
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /*
     * create a known series of knows relationships between predetermined users
     *
     * a <--knows--> b
     * a <--knows--> c
     * a <--knows--> d
     * b <--knows--> c
     * c <--knows--> d
     */
    @Before
    public void setUp() {
        a = conn.addUser(new User("userA", "noname", "user_a@email.com"));
        b = conn.addUser(new User("userB", "noname", "user_b@email.com"));
        c = conn.addUser(new User("userC", "noname", "user_c@email.com"));
        d = conn.addUser(new User("userD", "noname", "user_d@email.com"));
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);
        assertNotNull(d);
        invalidUserId = Integer.parseInt(d.getUserId()) + 1;
        assertTrue(conn.addUserKnows(a, b, 90));
        assertTrue(conn.addUserKnows(a, c, 80));
        assertTrue(conn.addUserKnows(a, d, 70));
        assertTrue(conn.addUserKnows(b, a, 60));
        assertTrue(conn.addUserKnows(c, a, 60));
        assertTrue(conn.addUserKnows(d, a, 60));
        assertTrue(conn.addUserKnows(b, c, 60));
        assertTrue(conn.addUserKnows(c, b, 60));
        assertTrue(conn.addUserKnows(c, d, 20));
        assertTrue(conn.addUserKnows(d, c, 20));
    }
    
    @After
    public void tearDown() {
        conn.deleteKnows(a, b);
        conn.deleteKnows(a, c);
        conn.deleteKnows(a, d);
        conn.deleteKnows(b, a);
        conn.deleteKnows(c, a);
        conn.deleteKnows(d, a);
        conn.deleteKnows(b, c);
        conn.deleteKnows(c, b);
        conn.deleteKnows(c, d);
        conn.deleteKnows(d, c);
    }

    /*
     * get all the users that user "a" knows
     * - expect a complete and full collection - and exect proper set
     */
    @Test
    public void getKnows() {
        assertEquals(conn.getKnows(Integer.parseInt(a.getUserId()), 91), Collections.EMPTY_LIST);
        Collection<User> users = conn.getKnows(Integer.parseInt(a.getUserId()), 60);
        assertEquals(users.size(), 3);
        if (users.contains(b)) {
            users.remove(b);
        }
        if (users.contains(c)) {
            users.remove(c);
        }
        if (users.contains(d)) {
            users.remove(d);
        }
        assertEquals(users.size(), 0);
    }
    
    /*
     * use a strength value above the known users
     * - expect null
     */
    @Test
    public void getKnowsInvalidStrength() {
        assertNull(conn.getKnows(Integer.parseInt(a.getUserId()), 99));
    }
    
    /*
     * use invalid user id
     * - expect null
     */
    @Test
    public void getKnowsInvalidUser() {
        assertNull(conn.getKnows(invalidUserId, 0));
    }
    
    /*
     * add a knows relationship
     *
     * b --knows:1--> d    
     * d --knows:100--> b
     *
     * - expect true;
     */
    @Test
    public void addKnows() {
        assertTrue(conn.addUserKnows(b, d, 1));
        assertTrue(conn.addUserKnows(d, b, 100));
    }
    
    /*
     * combinations of invalid user knows:
     * a) a knows a
     * b) blank user knows blank user
     * c) a knows blank user
     * d) blank user knows a
     * e) a knows b (already establish in @Before)
     *
     * - expect false in all cases
     */
    @Test
    public void addKnowsInvalid() {
        assertFalse(conn.addUserKnows(a, a, 1));
        assertFalse(conn.addUserKnows(new User(), new User(), 50));
        assertFalse(conn.addUserKnows(a, new User(), 50));
        assertFalse(conn.addUserKnows(new User(), a, 50));
        assertFalse(conn.addUserKnows(a, b, 50));
    }
    
    /*
     * update knows strength
     * - expect true
     */
    @Test
    public void updateKnows() {
        assertTrue(conn.updateUserKnows(a, b, 9));
        assertTrue(conn.updateUserKnows(a, c, 8));
        assertTrue(conn.updateUserKnows(a, d, 7));
        assertTrue(conn.updateUserKnows(b, a, 6));
        assertTrue(conn.updateUserKnows(c, a, 6));
        assertTrue(conn.updateUserKnows(d, a, 6));
        assertTrue(conn.updateUserKnows(b, c, 6));
        assertTrue(conn.updateUserKnows(c, b, 6));
        assertTrue(conn.updateUserKnows(c, d, 2));
        assertTrue(conn.updateUserKnows(d, c, 2));
    }
    
    /*
     * update knows, but with invalid combinations of user ids
     * - expect false
     */
    @Test
    public void updateKnowsInvalid() {
        assertFalse(conn.updateUserKnows(a, a, 1));
        assertFalse(conn.updateUserKnows(new User(), new User(), 50));
        assertFalse(conn.updateUserKnows(a, new User(), 50));
        assertFalse(conn.updateUserKnows(new User(), a, 50));
    }
    
    /*
     * retrieve users who know a certain user
     * - expect exact matching set
     */
    @Test
    public void getKnowsReverse() {
        assertEquals(conn.getKnowsReverse(Integer.parseInt(a.getUserId()), 100), Collections.EMPTY_LIST);
        Collection<User> users = conn.getKnowsReverse(Integer.parseInt(a.getUserId()), 60);
        assertEquals(users.size(), 3);
        if (users.contains(b)) {
            users.remove(b);
        }
        if (users.contains(c)) {
            users.remove(c);
        }
        if (users.contains(d)) {
            users.remove(d);
        }
        assertEquals(users.size(), 0);
    }
    
    /*
     * confirm that reverse knows returns null with invalid strength value
     * - expect null
     */
    @Test
    public void getKnowsReverseInvalidStrength() {
        assertNull(conn.getKnowsReverse(Integer.parseInt(a.getUserId()), 101));
    }
    
    /*
     * confirm that reverse knows returns null with invalid user id
     * - expect null
     */
    @Test
    public void getKnowsReverseInvalidUser() {
        assertNull(conn.getKnows(invalidUserId, 0));
    }
    
    /*
     * remove knows relationship from valid knows
     * - expect true
     */
    @Test
    public void deleteKnows() {
        assertTrue(conn.deleteKnows(a, b));
        assertTrue(conn.deleteKnows(a, c));
        assertTrue(conn.deleteKnows(a, d));
        assertTrue(conn.deleteKnows(b, a));
        assertTrue(conn.deleteKnows(c, a));
        assertTrue(conn.deleteKnows(d, a));
        assertTrue(conn.deleteKnows(b, c));
        assertTrue(conn.deleteKnows(c, b));
        assertTrue(conn.deleteKnows(c, d));
        assertTrue(conn.deleteKnows(d, c));
    }
    
    /*
     * remove knows relationship from invalid knows
     * - expect false
     */
    @Test
    public void deleteKnowsInvalid() {
        assertFalse(conn.deleteKnows(b, d));
        assertFalse(conn.deleteKnows(a, new User()));
        assertFalse(conn.deleteKnows(new User(), a));
        assertFalse(conn.deleteKnows(new User(), new User()));
    }
    
}
