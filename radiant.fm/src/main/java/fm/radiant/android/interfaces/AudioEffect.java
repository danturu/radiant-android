package fm.radiant.android.interfaces;

import fm.radiant.android.classes.player.Deck;

public abstract class AudioEffect {
    public void mount(Deck model) {

    }

    public abstract void beforeEffect();

    public abstract void afterEffect();
}
