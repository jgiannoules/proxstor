package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.testing.loader.*;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StressorMonitorWorker implements Runnable {

    private boolean go;
    UserRetrievalWorker urw;
    
    public static AtomicInteger usersProcessed = new AtomicInteger();
    public static AtomicInteger deviceCount = new AtomicInteger();
    public static AtomicInteger knownUsers = new AtomicInteger();
    
    public StressorMonitorWorker() {     
       this.go = true;
    }

    public void done() {
        this.go = false;
    }

    @Override
    public void run() {
    
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
            }
                
            System.out.println(usersProcessed.get() + " " + deviceCount.get() + " " + knownUsers.get());
        } while (this.go);
    }
}
