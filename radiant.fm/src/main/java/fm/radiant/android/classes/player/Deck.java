package fm.radiant.android.classes.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fm.radiant.android.interfaces.AudioModel;
import fm.radiant.android.interfaces.DeckEventListener;

public class Deck implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{
    private final Context context;
    private final MediaPlayer player;
    private final ScheduledExecutorService worker;

    private AudioModel currentAudio;
    private DeckEventListener currentEventListener;
    private float currentVolume;

    private final Stack<AudioModel> audioStack;
    private final List<Cue> cues;
    private final List<ScheduledFuture> cueTasks;

    public Deck(Context context) {
        this.context = context;
        this.worker  = Executors.newSingleThreadScheduledExecutor();

        this.player = new MediaPlayer();
        this.player.setOnPreparedListener(this);
        this.player.setOnCompletionListener(this);
        this.player.setOnErrorListener(this);

        this.audioStack = new Stack<AudioModel>();
        this.cues       = new ArrayList<Cue>();
        this.cueTasks   = new ArrayList<ScheduledFuture>();
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        currentEventListener.onReady(this, player, currentAudio);
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        if (audioStack.isEmpty()) {
            currentEventListener.onComplete(this, player);
        } else {
            prepareNext(context, audioStack.pop());
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

    public void load(final List<? extends AudioModel> models, final DeckEventListener eventListener) {
        this.currentEventListener = eventListener;
        resetStack(models);

        prepareNext(context, audioStack.pop());
    }

    public void load(AudioModel audio, final DeckEventListener deckEventListener) {
        load(Arrays.asList(audio), deckEventListener);
    }

    public void start() {
        if (currentAudio == null) return;

        enqueueCues(); player.start();
    }

    public void pause() {
        if (currentAudio == null) return;

        dequeueCues(); player.pause();
    }

    public void setCue(Cue cue) {
        cues.add(cue);
    }

    protected void enqueueCues() {
        int position = player.getCurrentPosition();
        int duration = player.getDuration();

        for (Cue cue : cues) {
            cueTasks.add(worker.schedule(cue, cue.getDelay(position, duration), TimeUnit.MILLISECONDS));
        }
    }

    protected void dequeueCues() {
        for (ScheduledFuture cueTask : cueTasks) { cueTask.cancel(true); }

        cueTasks.clear();
    }

    protected void prepareNext(Context context, AudioModel audio) {
        this.currentAudio  = audio;
        this.currentVolume = 1;

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

    private void resetStack(Collection<? extends AudioModel> models) {
        audioStack.clear(); audioStack.addAll(models);
    }
}
