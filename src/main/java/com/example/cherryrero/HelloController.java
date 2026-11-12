package com.example.cherryrero;
import java.io.IOException;
import javafx.event.ActionEvent;
import javazoom.jl.decoder.JavaLayerException;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;



public class HelloController {
    MusicThread t;

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
    protected void playRadio(){
        t.setRadioUrl(urlLink.getText());
        t.play();
    }


    public void pauseAudio(){

    }


    public void playAudio(ActionEvent actionEvent) {
    }

    public void nextAudio(ActionEvent actionEvent) {
    }
}