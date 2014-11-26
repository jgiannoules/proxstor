package com.giannoules.proxstor;

public class ProxStorUtil {
    
      /**
       * replace questionable chars in path with percent representation
       * 
       * @param path string representation of path to clean up
       * @return sanitized path
       */
      public static String cleanPath(String path) {
        String s;
        s = path.replaceAll("#", "%23");
        s = s.replaceAll(":", "%3A");
        return s;   
    }
      
}
