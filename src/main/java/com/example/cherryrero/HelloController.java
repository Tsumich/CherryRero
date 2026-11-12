package com.example.cherryrero;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javazoom.jl.decoder.JavaLayerException;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;



public class HelloController {
    public Label songTitle;
    MusicThread t;
    boolean isRadio;
    boolean isLocalAudioFile;

    public HelloController() throws IOException {
        t = new MusicThread();
        t.start();
        System.out.println("иниц контроллера");
    }

    @FXML
    private TextField urlLink;


//http://188.120.254.55:8000/ka128.mp3
//http://77.39.8.49:8000/vera_air_96
    @FXML
    protected void playAudio(){
        if(!urlLink.getText().isEmpty()){
            isRadio = true;
            t.setRadioUrl(urlLink.getText());
            t.play();
            songTitle.setText("Играет радио");
        }
    }


    public void pauseAudio(){
        if(isRadio){
            t.setRadioUrl(null);
            t.stopPlay();
        }

    }

    public void nextAudio(ActionEvent actionEvent) {
    }
}