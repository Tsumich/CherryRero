package com.example.cherryrero;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class MainController {
    public Label songTitle;
    public Button playButton;

    @FXML private ImageView currentProduct;
    @FXML private ComboBox<String> urlLink;

    private RotateTransition spin;
    private final RadioPlayer radioPlayer;

    // ---- СОСТОЯНИЕ МУЗЫКИ ------
    private enum PlayState {PLAYING_RADIO, PLAYING_LOCAL }
    private boolean is_playing = false;
    private PlayState currentState;
    int last_track_index;


    @FXML private Label folderPathLabel;
    @FXML private ListView<String> playlistView;

    @FXML private CheckMenuItem showLogsCheckItem;
    @FXML private VBox logPanel;
    @FXML private TextArea logArea;

    private final LocalMusicPlayer localPlayer = new LocalMusicPlayer();

    public MainController() {
        radioPlayer = new RadioPlayer();
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

        // Настройка анимации
        double radius = 100.0;
        currentProduct.setClip(new Circle(radius, radius, radius));
        spin = new RotateTransition(Duration.seconds(10), currentProduct);
        spin.setByAngle(360);
        spin.setCycleCount(RotateTransition.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);

        Platform.runLater(() -> {
            Stage stage = (Stage) urlLink.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                radioPlayer.stopPlay();
                localPlayer.stop();
            });
        });
    }

    @FXML
    protected void toggleLogsPanel() {
        boolean show = showLogsCheckItem.isSelected();
        logPanel.setVisible(show);
        logPanel.setManaged(show);
        if (logPanel.getScene() != null && logPanel.getScene().getWindow() != null) {
            logPanel.getScene().getWindow().sizeToScene();
        }
    }

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
        root.setPadding(new javafx.geometry.Insets(15));
        VBox.setVgrow(fullLogsArea, Priority.ALWAYS);

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
        is_playing = true;
        if (currentState == PlayState.PLAYING_RADIO) {
            radioPlayer.stopPlay();
            radioPlayer.setOldRadioUrl(null);
        }
        currentState = PlayState.PLAYING_LOCAL;
        last_track_index = playlistView.getSelectionModel().getSelectedIndex();
        if (last_track_index >= 0) {
            try {
                String title = localPlayer.playTrackAt(last_track_index);
                songTitle.setText(title);
                spin.play();
            } catch (LocalAudioException e) {
                songTitle.setText("Ошибка запуска файла");
            }
        }
    }

    @FXML
    protected void playAudio() throws LocalAudioException {
        String url = urlLink.getEditor().getText().trim();

        if (currentState == PlayState.PLAYING_RADIO && url.isEmpty()) {
            return;
        }

        if (!url.isEmpty()) {
            if(currentState == PlayState.PLAYING_RADIO){
                if (!urlLink.getItems().contains(url)) {
                    urlLink.getItems().add(0, url);
                    if (urlLink.getItems().size() > 10) {
                        urlLink.getItems().remove(10, urlLink.getItems().size());
                    }
                }
                songTitle.setText("Играет радио");
                LocalMusicPlayer.logAction("Запущено радио: " + url);
                radioPlayer.setRadioUrl(url);
                System.out.println(url);
                radioPlayer.play();
            }
        }
        if (currentState == PlayState.PLAYING_LOCAL) {
            System.out.println("возобн трек");
            localPlayer.playTrackAt(last_track_index);
        }
        spin.play();
    }

    public void pauseAudio() throws LocalAudioException {
        is_playing = !is_playing;
        System.out.println("pauseAudio: " + is_playing);
        if(!is_playing){
            if (currentState == PlayState.PLAYING_RADIO) {
                radioPlayer.setOldRadioUrl(null);
                radioPlayer.stopPlay();
            }
            else if (currentState == PlayState.PLAYING_LOCAL) {
                localPlayer.stop();
            }
            spin.pause();
        }
        else {
            System.out.println("unpauseAudio - else");
            playAudio();
        }
    }

    @FXML
    protected void nextAudio(ActionEvent actionEvent) {
        if (currentState == PlayState.PLAYING_LOCAL) {
            try {
                String title = localPlayer.playNext();
                if (title != null) {
                    playlistView.getSelectionModel().select(localPlayer.getCurrentIndex());
                    songTitle.setText(title);
                }
            } catch (LocalAudioException ignored) {}
        }
    }

    public void setRadioTrue(ActionEvent actionEvent) throws LocalAudioException {
        currentState = PlayState.PLAYING_RADIO;
        localPlayer.stop();
        is_playing = true;
        playAudio();
    }
}

//http://109.196.197.6:8010/DFM-anapa