package com.example.cherryrero;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class PropertiesReader {

    private static final String CONFIG_FILE_NAME = "player_config.properties";
    private static File configFile;

    // Ленивая инициализация пути (выполнится при первом обращении)
    private static File getConfigFile() {
        if (configFile == null) {
            String appData = System.getenv("APPDATA");
            if (appData == null) {
                appData = System.getProperty("user.home"); // Для Mac/Linux
            }
            File configDir = new File(appData, "CherryRero");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            configFile = new File(configDir, CONFIG_FILE_NAME);

            // Если файла нет — копируем шаблон из ресурсов (если он там есть)
            if (!configFile.exists()) {
                try (InputStream in = PropertiesReader.class.getResourceAsStream("/" + CONFIG_FILE_NAME)) {
                    if (in != null) {
                        Files.copy(in, configFile.toPath());
                    } else {
                        configFile.createNewFile();
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка создания конфига: " + e.getMessage());
                }
            }
        }
        return configFile;
    }

    public static String readFromProperties(String KEY) {
        File file = getConfigFile();
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            return props.getProperty(KEY);
        } catch (Exception e) {
            System.out.println("Ошибка чтения: " + e.getMessage());
            return "";
        }
    }

    public static void writeToProperties(String KEY, String VALUE) {
        File file = getConfigFile();
        Properties props = new Properties();

        // Читаем старые настройки, чтобы не затереть другие ключи
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (Exception e) {
                System.out.println("Ошибка чтения перед записью: " + e.getMessage());
            }
        }

        props.setProperty(KEY, VALUE);

        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "CherryRero settings");
        } catch (Exception e) {
            System.out.println("Ошибка записи: " + e.getMessage());
        }
    }
}