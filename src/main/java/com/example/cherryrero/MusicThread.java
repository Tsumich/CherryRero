package com.example.cherryrero;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class MusicThread extends Thread{

    private String radioUrl;
    boolean first_time = true;
    boolean next_song = true;
    Player player;

    @Override
    public void run() {
        System.out.println("MusicThread started");
    }
    URL url;
    InputStream is;
    InputStream fin;

    public void play(){
        try {
            if(!next_song || !first_time){
                 is.close();
                 first_time = false;
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

            var t = new Thread(task);
            t.start();

            next_song = !next_song;

        } catch (JavaLayerException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void stopPlay() {
        try{
            is.close();
            System.out.println("Stop radio");
        }catch (Exception ignored){

        }
    }
}
