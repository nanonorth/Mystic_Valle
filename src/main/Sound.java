package main;

import javax.sound.sampled.*;

public class Sound {
    public static void playWarp() {
        try {
            var stream  = Sound.class.getResourceAsStream("/sound/warp.mp3");
            if (stream == null) return;
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(stream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
