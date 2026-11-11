package com.example.cherryrero;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

@Getter
@Setter
@NoArgsConstructor
public class MusicThread implements Runnable{

    private String radioUrl;

    @Override
    public void run() {
        System.out.println("Пробуем найти аудиопоток");
        Console con = System.console();
        String urlString = radioUrl;
        try {
            URL url = new URL(urlString);
            InputStream fin = url.openStream();
            InputStream is = new BufferedInputStream(fin);

            Player player;
            player = new Player(is);
            player.play();
        }
        catch (FileNotFoundException e)
        {
            con.printf("Url %s не найден:", urlString);
        }
        catch (Exception e)
        {
            con.printf("При проигрывании с потока %s возникла следующая ошибка:", urlString);
            con.printf(e.toString());
        }
    }
}
