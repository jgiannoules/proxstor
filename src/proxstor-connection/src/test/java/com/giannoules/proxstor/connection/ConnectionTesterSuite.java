package com.giannoules.proxstor.connection;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Roll-up into one JUnit Test Suite for convenience
 * 
 * @author James Giannoules
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   DeviceConnectorTester.class,
   LocationConnectorTester.class,
   LocationNearbyConnectorTester.class,
   LocationWithinConnectorTester.class,
   EnvironmentalConnectorTester.class,
   UserConnectorTester.class,
   UserKnowsConnectorTester.class,
   LocalityConnectorTester.class
})
public class ConnectionTesterSuite {
  
}
