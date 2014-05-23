package fm.radiant.android.classes.player;

import java.util.concurrent.Callable;

public abstract class Cue implements Callable {
    private int time;

    public Cue(int time) {
        this.time = time;
    }

    public int getDelay(int position, int duration) {
        return time - position + (time < 0 ? duration : 0);
    }
}
