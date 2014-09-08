/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giannoules.proxstor.testing.loader;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * provides user progress/performance updates on "operations"
 * this routine does not know (or care) what an operation is
 * re-used by all the threaded loading routines
 */
public class MonitorWorker implements Runnable {

    private boolean go;
    private final AtomicInteger operations;

    public MonitorWorker() {
        operations = new AtomicInteger();
        operations.set(0);
        go = true;
    }

    public AtomicInteger getCounter() {
        return this.operations;
    }

    public void done() {
        this.go = false;
    }

    @Override
    public void run() {
        System.out.println("\t#\tops\tsince\ttime\tlatency\t\trate");
        System.out.println("\t-\t---\t-----\t----\t-------\t\t----");

        DecimalFormat df = new DecimalFormat("#.##");

        long startTime = System.currentTimeMillis();
        long last = startTime;
        long recent = 0;
        long n = 0;
        long threadId = Thread.currentThread().getId();
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
            }
            long now = System.currentTimeMillis();
            long d = now - last;
            if (((d >= 1000) && (operations.get() > 0)) || (!go)) {
                n++;
                int i = operations.get();
                recent = i - recent;
                double rate = (double) recent * 1000 / (double) d;
                double latency = (double) d / (double) recent;
                System.out.println("\t" + n + "\t" + i + "\t" + recent + "\t" + d + "\t" + df.format(latency) + "ms\t\t" + df.format(rate) + "/sec");
                recent = i;
                last = now;
            }
        } while (this.go);
    }
}
