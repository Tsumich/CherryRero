package com.example.cherryrero;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javazoom.jl.player.Player;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;

public class LocalMusicPlayer {
    private static final String CONFIG_FILE = "player_config.properties";
    private static final String FOLDER_KEY = "last_music_folder";
    private static final String LOG_FILE = "player_errors.log";
    private static final Logger logger = Logger.getLogger(LocalMusicPlayer.class.getName());
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Player jlayerPlayer;
    private Thread playThread;
    private List<File> playlist = new ArrayList<>();
    private int currentIndex = -1;

    // ссылка на тесктовое поле логов внизу окна
    private static TextArea uiLogArea;
    private static final StringBuilder sessionLogHistory = new StringBuilder();

    static {
        try {
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.WARNING);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Не удалось создать файл логов: " + e.getMessage());
        }
    }

    public static void setLogArea(TextArea area) {
        uiLogArea = area;
        if (uiLogArea != null) {
            uiLogArea.setText(sessionLogHistory.toString());
        }
    }

    // метод для вывода логов в консоль и в интерфейс
    public static void logAction(String message) {
        String formatted = "[" + LocalTime.now().format(TIME_FMT) + "] " + message + "\n";
        System.out.print("[LOG] " + formatted);
        sessionLogHistory.append(formatted);
        if (uiLogArea != null) {
            Platform.runLater(() -> uiLogArea.appendText(formatted));
        }
    }

    // чтение всех логов (текущих  + сохраненных ошибок из файла) для отдельного окна
    public static String getAllLogsText() {
        StringBuilder fullLogs = new StringBuilder();
        fullLogs.append("=== ЛОГИ ТЕКУЩЕЙ СЕССИИ ===\n");
        fullLogs.append(sessionLogHistory.isEmpty() ? "Пока нет действий.\n" : sessionLogHistory.toString());
        fullLogs.append("\n=== ЖУРНАЛ ОШИБОК ИЗ ФАЙЛА (").append(LOG_FILE).append(") ===\n");
        try {
            Path path = Path.of(LOG_FILE);
            if (Files.exists(path)) {
                String fileContent = Files.readString(path);
                fullLogs.append(fileContent.isEmpty() ? "Ошибок не зафиксировано.\n" : fileContent);
            } else {
                fullLogs.append("Файл ошибок еще не создавался.\n");
            }
        } catch (IOException e) {
            fullLogs.append("Не удалось прочитать файл логов: ").append(e.getMessage());
        }
        return fullLogs.toString();
    }

    public void saveFolderPath(String path) {
        Properties props = new Properties();
        props.setProperty(FOLDER_KEY, path);
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "CherryRero Saved Folder");
            logAction("Выбрана и сохранена папка: " + path);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при сохранении пути к папке: " + path, e);
            logAction("ОШИБКА сохранения пути к папке!");
        }
    }

    public String loadSavedFolderPath() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return null;
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            String savedPath = props.getProperty(FOLDER_KEY);
            logAction("Загружен сохраненный путь: " + savedPath);
            return savedPath;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка чтения файла настроек", e);
            logAction("ОШИБКА чтения файла настроек!");
            return null;
        }
    }

    public List<File> loadFolder(File folder) throws LocalAudioException {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            LocalAudioException ex = new LocalAudioException("Папка не существует или недоступна: " + folder);
            logger.log(Level.WARNING, ex.getMessage(), ex);
            logAction("ОШИБКА: Папка недоступна");
            throw ex;
        }

        playlist.clear();
        currentIndex = -1;
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if (files != null) {
            for (File f : files) {
                playlist.add(f);
            }
        }

        if (playlist.isEmpty()) {
            LocalAudioException ex = new LocalAudioException("В выбранной папке нет MP3 файлов!");
            logger.log(Level.WARNING, ex.getMessage());
            logAction("Предупреждение: В папке нет MP3 файлов");
            throw ex;
        }

        logAction("Найдено MP3 треков: " + playlist.size());
        return playlist;
    }

    public String playTrackAt(int index) throws LocalAudioException {
        if (playlist.isEmpty() || index < 0 || index >= playlist.size()) {
            throw new LocalAudioException("Трек не найден в списке");
        }
        currentIndex = index;
        File file = playlist.get(index);
        playFile(file);
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    public String playNext() throws LocalAudioException {
        if (playlist.isEmpty()) {
            return null;
        }
        int nextIndex = (currentIndex + 1) % playlist.size();
        logAction("Переключение на следующий трек");
        return playTrackAt(nextIndex);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    private void playFile(File file) throws LocalAudioException {
        if (file == null || !file.exists()) {
            LocalAudioException ex = new LocalAudioException("Аудиофайл не найден на устройстве!");
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            logAction("ОШИБКА: Файл не найден");
            throw ex;
        }

        stopSilent();

        playThread = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                logAction("Играет трек: " + file.getName());
                jlayerPlayer = new Player(bis);
                jlayerPlayer.play();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Ошибка воспроизведения файла: " + file.getName(), e);
                logAction("ОШИБКА воспроизведения: " + file.getName());
            }
        });
        playThread.setDaemon(true);
        playThread.start();
    }

    public void stop() {
        stopSilent();
        logAction("Воспроизведение остановлено");
    }

    private void stopSilent() {
        if (jlayerPlayer != null) {
            jlayerPlayer.close();
            jlayerPlayer = null;
        }
        if (playThread != null && playThread.isAlive()) {
            playThread.interrupt();
        }
    }

}