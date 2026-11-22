package com.example.cherryrero;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class PropertiesReader {

    public static String readFromProperties(String CONFIG_FILE, String KEY) {
        try {
            File file = new File(CONFIG_FILE);
            FileInputStream in = new FileInputStream(file);
            Properties props = new Properties();
            props.load(in);
            String value = props.getProperty(KEY);
            return value;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    public static void writeToProperties(String CONFIG_FILE, String KEY, String VALUE) {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);

        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        props.setProperty(KEY, VALUE);

        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "CherryRero settings");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}