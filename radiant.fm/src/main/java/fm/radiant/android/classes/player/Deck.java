package fm.radiant.android.classes.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fm.radiant.android.interfaces.AudioEffect;
import fm.radiant.android.interfaces.AudioModel;
import fm.radiant.android.interfaces.DeckEventListener;

public class Deck {
    private final MediaPlayer player;
    private final ScheduledExecutorService worker;

    private AudioModel currentAudio;
    private float currentVolume;

    private final List<Cue> cues;
    private final List<ScheduledFuture> cueTasks;

    public Deck() {
        this.player = new MediaPlayer();
        this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // this.player.setOnPreparedListener(this);
        // this.player.setOnCompletionListener(this);
        // this.player.setOnErrorListener(this);

        this.worker = Executors.newSingleThreadScheduledExecutor();

        this.cues     = new ArrayList<Cue>();
        this.cueTasks = new ArrayList<ScheduledFuture>();
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public AudioModel getAudio() {
        return currentAudio;
    }

    public void load(Context context, AudioModel audio, final DeckEventListener deckEventListener) {
        String filepath = audio.getFile(context).getAbsolutePath();

        player.reset();

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                deckEventListener.onReady(Deck.this, player);
            }
        });

        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                deckEventListener.onFailure(Deck.this, player);
                return false;
            }
        });

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                deckEventListener.onComplete(Deck.this, player);
            }
        });

        try {
            player.setDataSource(filepath);
        } catch (IOException e) {
            deckEventListener.onFailure(Deck.this, player);
        }

        player.prepareAsync();
    }

    public void start() {
        enqueueCues(); player.start();
    }

    public void pause() {
        dequeueCues(); player.pause();
    }

    public void setCue(Cue cue) {
        cues.add(cue);
    }

    private void enqueueCues() {
        int position = player.getCurrentPosition();
        int duration = player.getDuration();

        for (Cue cue : cues) {
            worker.schedule(cue, cue.getDelay(position, duration), TimeUnit.MILLISECONDS);
        }
    }

    private void dequeueCues() {
        for (ScheduledFuture cueTask : cueTasks) { cueTask.cancel(true); }

        cueTasks.clear();
    }
}
