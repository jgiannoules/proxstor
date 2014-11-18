package com.giannoules.proxstor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Useful debugging methods provided statically.
 * 
 * @author Jim Giannoules
 */
 
public class ProxStorDebug {
   
    /**
     * Simple mechanism to control debugging behavior in a global manner
     */
    private static final boolean DEBUG_ON = false;
    
    // stats on performance
    private static Map<String, AtomicLong> perfTotalTime = new ConcurrentHashMap<>();
    private static Map<String, AtomicInteger> perfTotalCount = new ConcurrentHashMap<>();
    
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
    
    public static long startTimer() {
        return System.currentTimeMillis();
    }
    
    public static long endTimer(String desc, long start) {
        long duration = System.currentTimeMillis() - start;
        if (perfTotalCount.get(desc) == null) {
            perfTotalCount.put(desc, new AtomicInteger());
        }
        perfTotalCount.get(desc).incrementAndGet();
        if (perfTotalTime.get(desc) == null) {
            perfTotalTime.put(desc, new AtomicLong());
        }
        perfTotalTime.get(desc).addAndGet(duration);
        return duration;
    }
    
    public static String getPerf() {
        StringBuilder sb = new StringBuilder();
        for (String k : perfTotalCount.keySet()) {
            sb.append(k);
            sb.append(": ");            
            sb.append(perfTotalCount.get(k).get());
            sb.append(", ");
            double avg = perfTotalTime.get(k).doubleValue() / perfTotalCount.get(k).doubleValue();
            sb.append(avg);
            sb.append("\n");
        }
        return sb.toString();
    }
}
