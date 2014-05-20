package fm.radiant.android.classes.player;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.File;
import java.io.IOException;

import fm.radiant.android.classes.syncer.Download;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Place;
import fm.radiant.android.models.Track;
import fm.radiant.android.receivers.MediaReceiver;
import fm.radiant.android.utils.AccountUtils;

/**
 * Created by kochnev on 14/05/14.
 */
public class Player {
    private static final String TAG = "Player";

    public static final int STATE_PLAYING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_WAITING = 3;
    public static final int STATE_SYNCING = 4;

    private Context context;
    private AlarmManager alarmManager;
    private Place place;

    private Period currentPeriod;
    private int currentState = STATE_STOPPED;

    final private Deck deckA = new Deck();
    final private Deck deckB = new Deck();
    final private Deck deckC = new Deck();

    public Player(Context context) {
        this.context      = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.place        = AccountUtils.getCurrentPlace();
    }

    public void play() {
        if (!currentPeriod.isNow()) {
            stop(STATE_WAITING);
            return;
        }

       new Thread() {
           public void run() {
               try {
                   deckA.inject(Player.this.getDirectory(), Player.this.getTrack());
                   deckA.play();
               } catch (IOException exception) {
                   Log.e(TAG, "eror", exception);
               }
           }
       }.start();
    }

    public void stop() {
        stop(STATE_STOPPED);

        deckA.stop();
        deckB.stop();
        deckC.stop();
    }

    protected File getDirectory() {
        return new File(context.getExternalFilesDir(null), Track.getDirectoryName());
    }

    protected Track getTrack() {
        return place.getTracks().get(5);
    }

    public void stop(int state) {
        setState(state);
    }

    public int getState() {
        return currentState;
    }

    private void setState(int state) {
        this.currentState = state;
    }

    public void enqueue() {
        if (place.getPeriods().isEmpty()) {
            return;
        }

        this.currentPeriod = Period.findCurrent(place.getPeriods());

        if (currentState != STATE_STOPPED) {
            play();
        }

        Log.d(TAG, "player");
        Log.d(TAG, "Time now" + new DateTime().toString());
        Log.d(TAG, "Start" + currentPeriod.getInterval().getStart().toString());
        Log.d(TAG, "End" + currentPeriod.getInterval().getEnd().toString());
        Log.d(TAG, Long.toString((currentPeriod.getDelay() -  System.currentTimeMillis()) / 1000 / 60) );

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, MediaReceiver.class), 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, currentPeriod.getDelay(), pendingIntent);
    }

}
