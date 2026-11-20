package com.example.cherryrero;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import java.io.*;
import java.net.URL;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RadioPlayer{

    private String radioUrl;
    private String oldRadioUrl;
    boolean first_time = true;
    boolean next_song = true;
    Player player;
    URL url;
    Thread playerThread;
    InputStream is;
    InputStream fin;

    public RadioPlayer() {
        System.out.println("RadioPlayer created");
    }

    public synchronized void play(){
        try {
            if(oldRadioUrl != null && Objects.equals(oldRadioUrl, radioUrl)) {
                System.out.println("sss");
                return;
            }
            oldRadioUrl = radioUrl;
            if(is != null){
                oldRadioUrl = radioUrl;
                player.close();
            }
            System.out.println("play " + radioUrl);
            URL url = new URL(radioUrl);
            fin = url.openStream();
            is = new BufferedInputStream(fin);
            player = new Player(is);


            Runnable task = ()->{
                try {
                    System.out.println("runnable start");
                    player.play();
                } catch (JavaLayerException e) {
                    System.out.println("Player finished with error");
                    player.close();
                }
                System.out.println("Player finished");
            };

            playerThread = new Thread(task);
            playerThread.start();

            next_song = !next_song;

        } catch (JavaLayerException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public synchronized void stopPlay() {
        if(player == null) return;
        player.close();
    }

    public synchronized void resume() {
        this.oldRadioUrl = null;
        play();
    }

}
