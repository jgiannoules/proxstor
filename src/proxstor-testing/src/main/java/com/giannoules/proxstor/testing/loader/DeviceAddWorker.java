/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giannoules.proxstor.testing.loader;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceAddWorker implements Runnable {

    private final User u;
    private final Device d;
    private final ProxStorConnector conn;
    private final AtomicInteger operations;

    public DeviceAddWorker(User u, Device d, ProxStorConnector conn, AtomicInteger operations) {
        this.u = u;
        this.d = d;
        this.conn = conn;
        this.operations = operations;
    }

    @Override
    public void run() {
        try {
            //d.setDevId(conn.addDevice(u.getUserId(), d).getDevId());
            Device newD = null;
            while (newD == null) {
                newD = conn.addDevice(u.getUserId(), d);
            }          
            d.setDevId(newD.getDevId());
            operations.getAndIncrement();
        } catch (Exception ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
