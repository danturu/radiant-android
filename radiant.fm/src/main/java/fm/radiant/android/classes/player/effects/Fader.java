package fm.radiant.android.classes.player.effects;

import fm.radiant.android.interfaces.AudioEffect;

public abstract class Fader extends AudioEffect {
    public Fader(final float from, final float to, final long delay) {

    }

    public Fader(final float to, final long delay) {

    }
}

/*

    public void fadeTo(final float from, final float to, final long duration, final Runnable callback) {
        if (fadeTask != null) fadeTask.cancel(true);

        this.currentVolume = from;
        player.setVolume(currentVolume, currentVolume);

        final float delta = (to - from) / (duration / 50);

        Runnable fade = new Runnable() {
            @Override
            public void run() {
                currentVolume += delta;

                if ((delta > 0 && currentVolume >= to) || (delta < 0 && currentVolume <= to))  {
                    currentVolume = to;

                    fadeTask.cancel(true);

                    if (callback != null) callback.run();
                }

                player.setVolume(currentVolume, currentVolume);
            }
        };

        fadeTask = fadeTimer.scheduleWithFixedDelay(fade, 0, 50, TimeUnit.MILLISECONDS);
    }

    public void fadeTo(final float to, final long delay, final Runnable callback) {
        fadeTo(currentVolume, to, delay, callback);
    }

 */