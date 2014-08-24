package com.giannoules.proxstor;

// @TODO document

public class ProxStorDebug {
    
    public static void println(String... msgs) {
        System.out.print("<< ProxStor Debug Print >> ");
        for (int x = 0; x < msgs.length - 1; x++)         
            System.out.print(msgs[x] + " ");
        if (msgs.length > 0)
            System.out.println(msgs[msgs.length - 1]);
    }
}
