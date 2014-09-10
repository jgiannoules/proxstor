package com.giannoules.proxstor.testing.loader;

import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import java.util.concurrent.atomic.AtomicInteger;

public class UserAddWorker implements Runnable {

    private final User u;
    private final ProxStorConnector conn;
    private final AtomicInteger operations;

    public UserAddWorker(User u, ProxStorConnector conn, AtomicInteger operations) {
        this.u = u;
        this.conn = conn;
        this.operations = operations;
    }

    @Override
    public void run() {
        u.setUserId(conn.addUser(u).getUserId());
        operations.getAndIncrement();
    }
}
