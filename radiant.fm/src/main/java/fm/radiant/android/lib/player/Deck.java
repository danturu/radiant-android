package fm.radiant.android.lib.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import fm.radiant.android.models.AudioModel;

public class Deck implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{
    private final int VALUE_ENDING_MARKER_SECONDS = 60;

    private final Context     mContext;
    private final MediaPlayer mPlayer = new MediaPlayer();

    private OnChangeListener mCurrentChangeListener;
    private AudioModel       mCurrentTrack;
    private float            mCurrentVolume = 1.0f;

    private final Stack<AudioModel> mTracks = new Stack<AudioModel>();
    private final List<Cue> mCues = new ArrayList<Cue>();

    private final Handler mTimer = new Handler();
    private final Handler mFader = new Handler();

    public Deck(Context context) {
        mContext = context;

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        mCurrentChangeListener.onReady(this, mCurrentTrack);
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        if (mTracks.isEmpty()) {
            mCurrentChangeListener.onEmpty(this);
        } else {
            prepare(mContext, mTracks.pop());
        }
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        mCurrentChangeListener.onFailure(this, mCurrentTrack, new IOException("Error code: " + extra));
        return false;
    }

    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    public AudioModel getAudio() {
        return mCurrentTrack;
    }

    public void load(final List<? extends AudioModel> tracks, OnChangeListener changeListener) {
        mCurrentChangeListener = changeListener;
        enqueue(tracks);

        if (tracks.isEmpty()) {
            mCurrentChangeListener.onEmpty(this);
        } else {
            prepare(mContext, mTracks.pop());
        }
    }

    public void load(AudioModel track, OnChangeListener changeListener) {
        load(Arrays.asList(track), changeListener);
    }

    public void eject() {
        mCurrentTrack = null;
        mTracks.clear();
        mPlayer.reset();
    }

    public synchronized void start() {
        if (mCurrentTrack == null) return;

        enqueueCues(); mPlayer.start();
    }

    public synchronized void pause() {
        if (mCurrentTrack == null || !mPlayer.isPlaying()) return;

        dequeueCues(); mPlayer.pause();
    }

    public synchronized void fade(final float from, final float to, final int duration, final Runnable callback) {
        Runnable faderIteration = new Runnable() {
            float delta = (to - from) / duration * 30;

            @Override
            public void run() {
                Deck.this.mCurrentVolume += delta;

                if (mPlayer.isPlaying() && ((delta > 0 && mCurrentVolume <= to) || (delta < 0 && mCurrentVolume >= to))) {
                    mFader.postDelayed(this, 30);
                } else {
                    mFader.post(callback);
                    Deck.this.mCurrentVolume = to;
                }

                mPlayer.setVolume(mCurrentVolume, mCurrentVolume);
            }
        };

        mCurrentVolume = from;

        mFader.removeCallbacksAndMessages(null);
        mFader.post(faderIteration);
    }

    public synchronized void fade(final float to, int duration, final Runnable callback) {
        fade(mCurrentVolume, to, duration, callback);
    }

    public void setVolume(float volume) {
        mCurrentVolume = volume; mPlayer.setVolume(volume, volume);
    }

    public void setCue(Cue cue) {
        mCues.add(cue);
    }

    public boolean isEnding() {
        return mPlayer.getDuration() - mPlayer.getCurrentPosition() < VALUE_ENDING_MARKER_SECONDS;
    }

    protected void enqueueCues() {
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();

        for (Cue cue : mCues) {
            mTimer.postDelayed(cue, cue.getDelay(position, duration));
        }
    }

    protected void dequeueCues() {
        mTimer.removeCallbacksAndMessages(null);
    }

    protected void prepare(Context context, AudioModel audio) {
        mCurrentTrack = audio;

        mPlayer.reset();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setVolume(mCurrentVolume, mCurrentVolume);

        try {
            mPlayer.setDataSource(audio.getSource(context));
            mPlayer.prepareAsync();

            mCurrentChangeListener.onQueue(this, mCurrentTrack);
        } catch (IOException exception) {
            mCurrentChangeListener.onFailure(this, mCurrentTrack, exception);
        }
    }

    private void enqueue(List<? extends AudioModel> tracks) {
        mTracks.clear();

        for (AudioModel track : tracks) {
            if (track != null) mTracks.add(track);
        }
    }

    public interface OnChangeListener {
        public void onReady(Deck deck, AudioModel audio);

        public void onQueue(Deck deck, AudioModel audio);

        public void onEmpty(Deck deck);

        public void onFailure(Deck deck, AudioModel audio, IOException exception);
    }
}