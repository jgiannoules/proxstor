package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.connection.ProxStorConnector;
import com.giannoules.proxstor.testing.ReaderWriter;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Stressor {

    private static final int USER_RETRIEVAL_WORKERS = 128;
    
    static List<String> userIds;
    static List<String> deviceIds;
    static List<String> locationIds;
    static List<String> sensorIds;

    static ProxStorConnector conn;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("must provide directory");
            return;
        }

        String dir = args[0];

        System.out.println("ProxStor Static Content Stressor");
        System.out.println("================================");
        System.out.println();

        userIds = ReaderWriter.read(dir + "/userIds.json", String.class);
        deviceIds = ReaderWriter.read(dir + "/devIds.json", String.class);
        locationIds = ReaderWriter.read(dir + "/locationIds.json", String.class);
        sensorIds = ReaderWriter.read(dir + "/sensorIds.json", String.class);
        System.out.println();

        conn = new ProxStorConnector("http://localhost:8080/api");

        StressorMonitorWorker smw = new StressorMonitorWorker();
        
        ExecutorService executor = Executors.newFixedThreadPool(USER_RETRIEVAL_WORKERS);
        for (int i = 0; i < USER_RETRIEVAL_WORKERS; i++) {
            UserRetrievalWorker worker = new UserRetrievalWorker(conn, userIds, smw);        
            executor.execute(worker);            
        }
        
        smw.run();   
    }

    private static String[] readFromFile(String dir, String file) {
        Gson gson = new Gson();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(dir + "/" + file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return gson.fromJson(sb.toString(), String[].class);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Stressor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Stressor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Stressor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

}
