package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.User;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class UserGenerator {
    
    /*
     * uniqueness of user tied to email address
     */
    private final Set<String> emailAddresses;
    private final Random random;
    static final String consonants = "bcdfghjklmnpqrstvwxyz";   
    static final String vowels = "aeiou";
    
    public UserGenerator() {
        this.emailAddresses = new HashSet<>();
        this.random = new Random();
    }
    
    public User genUser() {
        User u = new User();
        do {
            u.setFirstName(randomFirstName());
            u.setLastName(randomLastName());
            u.setEmail(randomEmail(u.getFirstName(), u.getLastName()));
        } while (emailAddresses.contains(u.getEmail()));
        emailAddresses.add(u.getEmail());
        return u;
    }
    
    private boolean unique(User u) {
        return (!emailAddresses.contains(u.getEmail()));
    }
    
    private boolean unique(String email) {
        return (!emailAddresses.contains(email));
    }
    
    /*
     * combine elements of first name, last name, number, and
     * selection of domains to generate email address
     */
    private String randomEmail(String first, String last) {
        StringBuilder sb = new StringBuilder();
        String s, t, sep = "";
               
        if (random.nextBoolean()) {
            s = first;
        } else {
            s = first.substring(0, 1);
        }
        if (random.nextBoolean()) {
            t = last;
        } else {
            t = last.substring(0, 1);
        }       
        switch (random.nextInt(4)) {
            case 0:     sep = ".";
                        break;
            case 1:     sep = "_";
                        break;
            case 2:     sep = "-";
                        break;
            case 3:     sep = "";
                        break;
        }
        if (random.nextBoolean()) {
            sb.append(s).append(sep).append(t);
        } else {
            sb.append(t).append(sep).append(s);
        }
        if (random.nextInt(10) > 3) {
            sb.append(Integer.toString(random.nextInt(1000000)));
        }
        switch (random.nextInt(10)) {
            case 0:
            case 1:
            case 2:     s = "email.com";
                        break;
            case 3:     s = "nospam.net";
                        break;
            case 4:
            case 5:     s = "aol.com";
                        break;
            case 6:
            case 7:
            case 8:     s = "lightspeed.net";
                        break;
            case 9:     s = "goggles.com";
                        break;
        }
        sb.append("@").append(s);
        return sb.toString();
    }
    
    private String randomName(int len) {
        boolean pv = false;
        StringBuilder sb = new StringBuilder();
        int mod = 2;
        for (int i = 0; i < len; i++) {           
            if (random.nextInt() % mod == 0) {
                sb.append(vowels.charAt(random.nextInt(vowels.length())));
                pv = true;
                mod = 10;
            } else {               
                sb.append(consonants.charAt(random.nextInt(consonants.length())));
                pv = false;
                mod = 2;
            }
        }
        return sb.toString();
    }
    
    private String randomFirstName() {
        String name = randomName(random.nextInt(4) + 4);
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    private String randomLastName() {
        String name = randomName(random.nextInt(6) + 5);
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
