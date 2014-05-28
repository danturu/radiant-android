package fm.radiant.android.interfaces;

import android.media.MediaPlayer;

import java.io.IOException;

import fm.radiant.android.classes.player.Deck;
import fm.radiant.android.lib.AudioModel;

public interface DeckEventListener {
    public void onReady(Deck deck, MediaPlayer player, AudioModel audio);

    public void onNext(Deck deck, MediaPlayer player, AudioModel audio);

    public void onFailure(Deck deck, MediaPlayer player, AudioModel audio, IOException exception);

    public void onComplete(Deck deck, MediaPlayer player);
}