/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giannoules.proxstor.testing.loader;

import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.concurrent.atomic.AtomicInteger;

public class UserKnowsWorker implements Runnable {

    private final User u;
    private final User v;
    private final int strength;
    private final ProxStorConnector conn;
    private final AtomicInteger operations;

    public UserKnowsWorker(User u, User v, int strength, ProxStorConnector conn, AtomicInteger operations) {
        this.u = u;
        this.v = v;
        this.strength = strength;
        this.conn = conn;
        this.operations = operations;
    }

    @Override
    public void run() {
        conn.addUserKnows(u, v, strength);
        operations.getAndIncrement();
    }
}
