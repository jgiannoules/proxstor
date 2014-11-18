package com.giannoules.proxstor.admin.testing;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.device.DeviceDao;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.UserAlreadyKnowsUser;
import com.giannoules.proxstor.knows.KnowsDao;
import com.giannoules.proxstor.user.UserDao;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserWorker implements Runnable {

    private static final int MAX_USER_KNOWS = 1;
    
    private Integer count;
    private final Random r;
    private final UserGenerator ug;
    private final DeviceGenerator dg;
    private final List<User> userPool;
    private final boolean leanMode;

    public UserWorker(Integer count, boolean leanMode) {
        this.r = new Random();
        this.ug = new UserGenerator(r);
        this.dg = new DeviceGenerator(r);
        this.count = count;
        this.userPool = new ArrayList<>();
        this.leanMode = leanMode;
    }

    @Override
    public void run() {
        User u, v;
        Device d, e;
        Integer n;
        Set<User> tmpUsers = new HashSet<>();

        while (count > 0) {

            // generate and add new user
            u = ug.genUser();
            v = null;
            while (v == null) {
                v = UserDao.instance.add(u);
                if (v == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(com.giannoules.proxstor.testing.report.UserWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            u.setUserId(v.getUserId());
            
            if (!leanMode) {
                userPool.add(u);

                // going to have a friend, or two, or more?
                if (r.nextBoolean() && userPool.size() > 0) {
                    n = r.nextInt(MAX_USER_KNOWS) + 1;
                    if (n > userPool.size()) {
                        n = userPool.size();
                    }
                    //ProxStorDebug.println("n: " + n);
                    tmpUsers.clear();
                    while (n-- > 0) {
                        tmpUsers.add(userPool.get(r.nextInt(userPool.size())));
                    }
                    for (User x : tmpUsers) {
                        try {
                            do {
                            } while (KnowsDao.instance.addKnows(u.getUserId(), x.getUserId(), r.nextInt(100) + 1));
                        } catch (InvalidUserId | UserAlreadyKnowsUser ex) {
                            //Logger.getLogger(UserWorker.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                // randomly take out a user
                if (userPool.size() > MAX_USER_KNOWS) {                
                    userPool.remove(r.nextInt(userPool.size()));
                }

                d = dg.genDevice();

                e = null;
                while (e == null) {
                    try {
                        e = DeviceDao.instance.add(u.getUserId(), d);
                        if (e == null) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(com.giannoules.proxstor.testing.report.UserWorker.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (InvalidUserId ex) {
                        Logger.getLogger(UserWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                d.setDevId(e.getDevId());
            }

            count--;
        }
    }
}
