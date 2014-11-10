package com.giannoules.proxstor;

public class ProxStorUtil {
    
      public static String cleanPath(String path) {
        String s;
        s = path.replaceAll("#", "%23");
        s = s.replaceAll(":", "%3A");
        return s;   
    }
      
}
