package com.example.cherryrero;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class HelloController {
    public Label songTitle;
    MusicThread t;
    boolean isRadio;
    boolean isLocalAudioFile;

    @FXML
    private TextField urlLink;
    @FXML
    private Label folderPathLabel;
    @FXML
    private ListView<String> playlistView;

    // элементы для окна и панели логов
    @FXML
    private CheckMenuItem showLogsCheckItem;
    @FXML
    private VBox logPanel;
    @FXML
    private TextArea logArea;

    private final LocalMusicPlayer localPlayer = new LocalMusicPlayer();

    public HelloController() throws IOException {
        t = new MusicThread();
        t.start();
        LocalMusicPlayer.logAction("Инициализация приложения CherryRero");
    }

    @FXML
    public void initialize() {
        LocalMusicPlayer.setLogArea(logArea);
        String savedPath = localPlayer.loadSavedFolderPath();
        if (savedPath != null) {
            File folder = new File(savedPath);
            if (folder.exists() && folder.isDirectory()) {
                updatePlaylist(folder);
            }
        }
    }

    // меню "вид" - "отброжать логи"
    @FXML
    protected void toggleLogsPanel() {
        boolean show = showLogsCheckItem.isSelected();
        logPanel.setVisible(show);
        logPanel.setManaged(show);
        if (logPanel.getScene() != null && logPanel.getScene().getWindow() != null) {
            logPanel.getScene().getWindow().sizeToScene();
        }
    }

    // меню "файл" - "посмотреть логи"
    @FXML
    protected void openLogsWindow() {
        Stage logStage = new Stage();
        logStage.setTitle("Журнал логов — CherryRero");

        TextArea fullLogsArea = new TextArea(LocalMusicPlayer.getAllLogsText());
        fullLogsArea.setEditable(false);
        fullLogsArea.setWrapText(true);
        fullLogsArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> fullLogsArea.setText(LocalMusicPlayer.getAllLogsText()));

        VBox root = new VBox(10, fullLogsArea, refreshBtn);
        root.setPadding(new Insets(15));
        VBox.setVgrow(fullLogsArea, javafx.scene.layout.Priority.ALWAYS);

        logStage.setScene(new Scene(root, 550, 400));
        logStage.show();
    }

    @FXML
    protected void chooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Выберите папку с музыкой (.mp3)");
        File selectedDir = chooser.showDialog(playlistView.getScene().getWindow());

        if (selectedDir != null) {
            localPlayer.saveFolderPath(selectedDir.getAbsolutePath());
            updatePlaylist(selectedDir);
        }
    }

    private void updatePlaylist(File folder) {
        folderPathLabel.setText("Папка: " + folder.getName());
        playlistView.getItems().clear();
        try {
            List<File> songs = localPlayer.loadFolder(folder);
            for (File song : songs) {
                playlistView.getItems().add(song.getName());
            }
        } catch (LocalAudioException e) {
            folderPathLabel.setText(e.getMessage());
            songTitle.setText("MP3 файлы не найдены");
        }
    }

    @FXML
    protected void onTrackSelected(MouseEvent event) {
        int index = playlistView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            if (isRadio) {
                t.stopPlay();
                isRadio = false;
            }
            try {
                String title = localPlayer.playTrackAt(index);
                isLocalAudioFile = true;
                songTitle.setText(title);
            } catch (LocalAudioException e) {
                songTitle.setText("Ошибка запуска файла");
            }
        }
    }

    //http://188.120.254
    //http://77.39.8
    @FXML
    protected void playAudio(){
        if(!urlLink.getText().isEmpty()){
            if(isLocalAudioFile){
                localPlayer.stop();
                isLocalAudioFile = false;
            }
            isRadio = true;
            t.setRadioUrl(urlLink.getText());
            t.play();
            songTitle.setText("Играет радио");
            LocalMusicPlayer.logAction("Запущено радио: " + urlLink.getText());
        }
    }

    public void pauseAudio(){
        if(isRadio){
            t.setRadioUrl(null);
            t.stopPlay();
            isRadio = false;
            LocalMusicPlayer.logAction("Радио остановлено");
        }
        if(isLocalAudioFile){
            localPlayer.stop();
            isLocalAudioFile = false;
        }
    }

    public void nextAudio(ActionEvent actionEvent) {
        if (isLocalAudioFile) {
            try {
                String title = localPlayer.playNext();
                if (title != null) {
                    playlistView.getSelectionModel().select(localPlayer.getCurrentIndex());
                    songTitle.setText(title);
                }
            } catch (LocalAudioException ignored) {
            }
        }
    }
}
