package com.giannoules.proxstor;

/**
 * Useful debugging methods provided statically.
 * 
 * @author Jim Giannoules
 */
 
public class ProxStorDebug {
   
    /**
     * Simple mechanism to control debugging behavior in a global manner
     */
    private static boolean DEBUG_ON = false;
    
    /**
     * Print debug string prepended with debug text
     * 
     * @param msgs Message to print
     */
    public static void println(String... msgs) {
        if (!DEBUG_ON) {
            return;
        }
        System.out.print("<< ProxStor Debug Print >> ");
        for (int x = 0; x < msgs.length - 1; x++)         
            System.out.print(msgs[x] + " ");
        if (msgs.length > 0)
            System.out.println(msgs[msgs.length - 1]);
    }
}
