package com.giannoules.proxstor.connection;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * roll-up into one JUnit Test Suite for convienence
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   DeviceConnectorTester.class,
   LocationConnectorTester.class,
   LocationNearbyConnectorTester.class,
   LocationWithinConnectorTester.class,
   SensorConnectorTester.class,
   UserConnectorTester.class,
   UserKnowsConnectorTester.class
})
public class ConnectionTesterSuite {
  
}
