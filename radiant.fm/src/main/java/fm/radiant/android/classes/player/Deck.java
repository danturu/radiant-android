package fm.radiant.android.classes.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;

import fm.radiant.android.interfaces.Audioable;

public class Deck {
    private final MediaPlayer player;

    Audioable audio;

    public Deck() {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void inject(File directory, Audioable audio) throws IOException {
        this.audio = audio;

        player.setDataSource(Deck.this.audio.getFile(directory).getAbsolutePath());
        player.prepare();
    }

    public void eject() {
    }

    public void play() {
        player.start();
    }

    public void stop() {

    }
}
