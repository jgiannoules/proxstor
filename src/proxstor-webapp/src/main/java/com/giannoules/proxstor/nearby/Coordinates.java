package com.giannoules.proxstor.nearby;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.api.Location;
import static com.tinkerpop.blueprints.Compare.GREATER_THAN_EQUAL;
import static com.tinkerpop.blueprints.Compare.LESS_THAN_EQUAL;
import java.util.ArrayList;
import java.util.List;

public class Coordinates {

    private static double R = 6372797.560856;
    
    public Double latitude;
    public Double longitude;
    
    public Coordinates() {
        latitude = null;
        longitude = null;
    }
    
    public Coordinates(Double latitude, Double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
    
    public Coordinates(Location loc) {
        this.longitude = loc.getLongitude();
        this.latitude = loc.getLatitude();
    }

    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Double distanceFrom(Coordinates other) {
      /*
       * haversign
       * from http://www.codecodex.com/wiki/Calculate_distance_between_two_points_on_a_globe#Java
       */
      double R = 6372797.560856;
      double lat1 = this.latitude;  
      double lat2 = other.getLatitude();  
      double lon1 = this.longitude;  
      double lon2 = other.getLongitude();  
      double dLat = Math.toRadians(lat2-lat1);  
      double dLon = Math.toRadians(lon2-lon1);  
      double a = Math.sin(dLat/2) * Math.sin(dLat/2) +  
         Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *  
         Math.sin(dLon/2) * Math.sin(dLon/2);  
      double c = 2 * Math.asin(Math.sqrt(a));
      double distance = R * c;
      
      return distance;
    }
    
    public List<Coordinates> boundingBox(Double d) {
        
        List<Coordinates> coordinates = new ArrayList<>();
        Double lat;
        Double lon;
        Double bearing;
        
        // convert distance (d) into hypotenuse of bounding box
        d = Math.sqrt(2*(d*d));
                       
        bearing = Math.toRadians(315);
        lat = Math.toDegrees((d/R) * Math.cos(bearing)) + latitude;
        lon = Math.toDegrees((d/(R * Math.sin(Math.toRadians(lat)))) * Math.sin(bearing)) + longitude;
        coordinates.add(new Coordinates(lat, lon));
        
        bearing = Math.toRadians(135);
        lat = Math.toDegrees((d/R) * Math.cos(bearing)) + latitude;
        lon = Math.toDegrees((d/(R * Math.sin(Math.toRadians(lat)))) * Math.sin(bearing)) + longitude;
        coordinates.add(new Coordinates(lat, lon));
        
        return coordinates;     
    }
    
}
