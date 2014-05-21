package fm.radiant.android.classes.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fm.radiant.android.interfaces.Audioable;

public class Deck {
    private final MediaPlayer player;

    public static final int IDLE    = 1;
    public static final int LOADING = 2;
    public static final int PLAYING = 3;
    public static final int PAUSED  = 4;
    public static final int STOPPED = 5;

    private final ScheduledExecutorService fadeWorker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture fadeTask = null;

    Handler handler = new Handler();
    private Audioable audio = null;

    List<Cue> cues = new ArrayList<Cue>();

    float currentVolume = 0.0f;
    int currentState = IDLE;

    public Deck() {
        player = new MediaPlayer();
    }

    public void inject(File directory, Audioable audio) throws IOException {
        eject();

        this.audio = audio;

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
      //  player.setDataSource(audio.getFile().getAbsolutePath());
        player.prepare();
        player.setVolume(0f, 0f);

        currentVolume = 0.0f;
    }

    public void eject() {
        player.reset();
    }

    public void play() {
        for (Cue cue : cues) {
            handler.postDelayed(cue.getCallback(), cue.getDelay(player.getCurrentPosition(), player.getCurrentPosition()));
        }

        Log.d("sd", "play");

        try {
            player.start();
            currentState = PLAYING;
            fadeTo(1.0f, 3000, null);
        } catch (IllegalStateException e) {

        }
    }

    public void pause() {
        handler.removeCallbacksAndMessages(null);

        Log.d("p", "puase");

        fadeTo(0.0f, 3000, new Runnable() {
            @Override
            public void run() {
                try {
                    player.pause();
                    currentState = PAUSED;
                } catch (IllegalStateException e) {

                }
            }
        });
    }

    public void setCue(Cue cue) {
        cues.add(cue);
    }

    public void fadeTo(final float from, final float to, final long delay, final Runnable callback) {
        if (fadeTask != null && !fadeTask.isDone()) {
            fadeTask.cancel(true);
        }

        final float delta = (to - from) / (delay / 50);

        Log.d("delta", Float.toString(delta));

        currentVolume = from;
        player.setVolume(currentVolume, currentVolume);

        Runnable fadeIn = new Runnable() {
            @Override
            public void run() {
                currentVolume += delta;

                if ((delta == 0.0f) || (delta > 0.0f && currentVolume >= to) || (delta < 0.0f && currentVolume <= to))  {
                    currentVolume = to;

                    fadeTask.cancel(true);

                    if (callback != null) callback.run();
                }

                player.setVolume(currentVolume, currentVolume);
            }
        };

        fadeTask = fadeWorker.scheduleWithFixedDelay(fadeIn, 0, 50, TimeUnit.MILLISECONDS);
    }

    public void fadeTo(final float to, final long delay, final Runnable callback) {
        fadeTo(currentVolume, to, delay, callback);
    }

    public Audioable getAudio() {
        return audio;
    }
}
