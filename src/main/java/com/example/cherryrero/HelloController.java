package com.example.cherryrero;
import java.net.URL;

import javafx.event.ActionEvent;
import javazoom.jl.player.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;



public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private TextField urlLink;

    boolean isPlaying = false;
    boolean isPause = false;

    MusicThread musicThread = new MusicThread();

    @FXML
    protected void playRadio() {
        System.out.println("Вы нажали кнопку");
        isPlaying = !isPlaying;
        if(isPlaying){
            musicThread.run();
        }


    }


    public void pauseAudio(ActionEvent actionEvent) {
        isPlaying = false;
    }
}