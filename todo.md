## ProxStor TODO List

* stop duplicating Knows relationships with @POST. Get @PUT working.
* /search/*/ with invalid IDs should not crash
* static model tests
* static model
* indexing
* stopping/resume graph instance
* pre-defined static loads
* wiki into shape
* knows edge w/strength
* nearby edge w/distance (meters)
* rename Sensor
* add capabilities (sensors?) to Device
* should Devices change users?
* model Locations within Locations
* Locations need types (building, geoarea, municipality, ?)
* Sensor types need to be expanded
* Location +address field
* Location +lat/long
* Location search radius
  * current location
  * specified location
  * specified lat/long
* review Dao flow (seems unoptimized)
  * exception throwing boundaries 
