package fm.radiant.android.classes.player;

public abstract class Cue implements Runnable {
    private int time;

    public Cue(int time) {
        this.time = time;
    }

    public int getDelay(int position, int duration) {
        return time - position + (time < 0 ? duration : 0);
    }
}
