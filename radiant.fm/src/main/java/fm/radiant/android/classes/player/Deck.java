package fm.radiant.android.classes.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import fm.radiant.android.interfaces.DeckEventListener;
import fm.radiant.android.lib.AudioModel;

public class Deck implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{
    private final Context context;
    private final MediaPlayer player;

    private AudioModel currentAudio;
    private DeckEventListener currentEventListener;
    private float currentVolume;

    private final Stack<AudioModel> tracks;
    private final List<Cue> cues;

    private final Handler timer = new Handler();
    private final Handler fader = new Handler();

    public Deck(Context context) {
        this.context = context;

        this.player = new MediaPlayer();
        this.player.setOnPreparedListener(this);
        this.player.setOnCompletionListener(this);
        this.player.setOnErrorListener(this);

        this.tracks = new Stack<AudioModel>();
        this.cues   = new ArrayList<Cue>();
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        currentEventListener.onReady(this, player, currentAudio);
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        if (tracks.isEmpty()) {
            currentEventListener.onComplete(this, player);
        } else {
            prepareNext(context, tracks.pop());
        }
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        currentEventListener.onFailure(this, player, currentAudio, new IOException("Error code: " + extra));
        return false;
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public float getVolume() {
        return currentVolume;
    }

    public AudioModel getAudio() {
        return currentAudio;
    }

    public void load(final List<? extends AudioModel> tracks, final DeckEventListener eventListener) {
        this.currentEventListener = eventListener;
        resetStackWith(tracks);

        if (tracks.isEmpty()) {
            currentEventListener.onComplete(this, player);
        } else {
            prepareNext(context, this.tracks.pop());
        }
    }

    public void load(AudioModel track, final DeckEventListener deckEventListener) {
        load(Arrays.asList(track), deckEventListener);
    }

    public void start() {
        if (currentAudio == null) return;

        enqueueCues(); player.start();
    }

    public void pause() {
        if (currentAudio == null || !player.isPlaying()) return;

        dequeueCues(); player.pause();
    }

    public void fade(final float from, final float to, final int duration, final Runnable callback) {
        Runnable faderIteration = new Runnable() {
            float delta = (to - from) / duration * 30;

            @Override
            public void run() {
                Deck.this.currentVolume += delta;

                if (player.isPlaying() && ((delta > 0 && currentVolume <= to) || (delta < 0 && currentVolume >= to))) {
                    fader.postDelayed(this, 30);
                } else {
                    fader.post(callback);
                }

                player.setVolume(currentVolume, currentVolume);
            }
        };

        this.currentVolume = from;

        fader.removeCallbacksAndMessages(null);
        fader.post(faderIteration);
    }

    public void fade(final float to, int duration, final Runnable callback) {
        fade(currentVolume, to, duration, callback);
    }

    public void setCue(Cue cue) {
        cues.add(cue);
    }

    protected void enqueueCues() {
        int position = player.getCurrentPosition();
        int duration = player.getDuration();

        for (Cue cue : cues) {
            timer.postDelayed(cue, cue.getDelay(position, duration));
        }
    }

    protected void dequeueCues() {
        timer.removeCallbacksAndMessages(null);
    }

    protected void prepareNext(Context context, AudioModel audio) {
        this.currentAudio  = audio;
        this.currentVolume = 1.0f;

        player.reset();
        player.setVolume(currentVolume, currentVolume);

        try {
            player.setDataSource(audio.getSource(context));
            player.prepareAsync();

            currentEventListener.onNext(this, player, currentAudio);
        } catch (IOException exception) {
            currentEventListener.onFailure(this, player, currentAudio, exception);
        }
    }

    private void resetStackWith(List<? extends AudioModel> tracks) {
        this.tracks.clear();

        for (AudioModel track : tracks) {
            if (track != null) this.tracks.add(track);
        }
    }
}
