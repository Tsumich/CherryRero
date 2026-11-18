package com.example.cherryrero;
import javafx.scene.media.Media;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


public class MusicThread {
    private MediaPlayer player;

    public void pause() {
        if (player != null) player.pause();
    }

    public void resume() {
        if (player != null) player.play();
    }
    public void play(String url) {
        if (player != null) {
            player.stop();
            player.dispose();
            player = null;
        }
        try {
            Media media = new Media(url);
            player = new MediaPlayer((media));
            player.play();
        } catch (Exception e) {
            e.printStackTrace();
            player = null;
        }
    }

    public void stopPlay() {
        if (player != null) {
            player.stop();
        }
    }
}
