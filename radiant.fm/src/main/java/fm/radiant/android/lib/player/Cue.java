package fm.radiant.android.lib.player;

import android.util.Log;

public abstract class Cue implements Runnable {
    private int mTime;

    public Cue(int time) {
        mTime = time;
    }

    public int getDelay(int position, int duration) {
        Log.d("time",   ""+ (mTime - position + (mTime < 0 ? duration : 0)) +"/" + position +"/" + duration);
        return mTime - position + (mTime < 0 ? duration : 0);
    }
}