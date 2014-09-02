package com.giannoules.proxstor.testing.stressor;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * encapsulate logic to read/write lists of JSON objects
 * using Google's GSON library
 */
public class ReaderWriter<T> {
    
    public static <T> boolean write(String filename, List<T> objs) {
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();
        String json = gson.toJson(objs);        
        try {                 
            FileWriter writer = new FileWriter(filename);            
            writer.write(json);
            writer.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
        
    public static <T> List<T> read(String filename, Class c) {      
        List<T> objs = new ArrayList();
        BufferedReader br;
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();
        try {
            br = new BufferedReader(new FileReader(filename));
            JsonElement json = new JsonParser().parse(br);
            JsonArray array = json.getAsJsonArray();
            for (JsonElement json2 : array) {                
                objs.add((T) gson.fromJson(json2, c));                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
       return objs;        
    }
      
}