package com.giannoules.proxstor.testing.dynamicstressor;

import com.giannoules.proxstor.testing.loader.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StressorMonitorWorker implements Runnable {

    private boolean go;
    List<AtomicInteger> counters;
    List<String> descriptions;
    
    public StressorMonitorWorker() {     
       this.go = true;
       counters = new ArrayList<>();
       descriptions = new ArrayList<>();
    }
    
    public AtomicInteger register(String descr) {
        AtomicInteger counter = new AtomicInteger();
        counters.add(counter);
        descriptions.add(descr);
        return counter;
    }

    public void done() {
        this.go = false;
    }

    @Override
    public void run() {
    
        DecimalFormat df = new DecimalFormat("#.##");
        long pass = 0;
        double rate, avg;
        Integer grand_total = 0;
        
        do {
            long before = System.currentTimeMillis();
            Integer total = 0;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {     // just print earlier... it's ok
                Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
            }            
            long d = System.currentTimeMillis() - before;
            
            System.out.println("[ pass: " + pass + "\t]===============================");
      
             for (int i = 0; i < counters.size(); i++) {
                Integer count = counters.get(i).getAndSet(0);
                rate =  count * 1000 / d;
                total += count;                
                System.out.println(descriptions.get(i) + "\t" + df.format(rate) + "\tops/sec");
             }      
             
            System.out.println();
            System.out.println("Total:\t\t\t\t" + total);
            if (pass > 25) {
                    grand_total += total;
                    avg = (double) grand_total / (double) (pass - 25);
                    System.out.println("Avg:\t\t\t\t" + avg);
            }
            System.out.println("\n");
            pass++;
        } while (this.go);
    }
}
