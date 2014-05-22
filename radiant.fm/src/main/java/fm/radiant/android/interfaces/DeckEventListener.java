package fm.radiant.android.interfaces;

import android.media.MediaPlayer;

import fm.radiant.android.classes.player.Deck;

public interface DeckEventListener {
    public void onReady(Deck deck, MediaPlayer player);

    public void onFailure(Deck deck, MediaPlayer player);

    public void onComplete(Deck deck, MediaPlayer player);
}