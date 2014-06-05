package fm.radiant.android.lib.player;

public abstract class Cue implements Runnable {
    private int mTime;

    public Cue(int time) {
        mTime = time;
    }

    public int getDelay(int position, int duration) {
        return mTime - position + (mTime < 0 ? duration : 0);
    }
}
