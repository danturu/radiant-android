package fm.radiant.android.classes.player;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import fm.radiant.android.classes.indexer.AdsIndexer;
import fm.radiant.android.classes.indexer.TracksIndexer;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Track;
import fm.radiant.android.receivers.MediaReceiver;
import fm.radiant.android.utils.LibraryUtils;

public class Player {
    private static final String TAG = "Player";

    public static final int STATE_PLAYING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_WAITING = 3;
    public static final int STATE_SYNCING = 4;

    private int currentState = STATE_STOPPED;

    private final Context context;
    private final AlarmManager alarmManager;
    private final ScheduledExecutorService adsTimer = Executors.newSingleThreadScheduledExecutor();

    private final Deck deckA = new Deck();
    private final Deck deckB = new Deck();
    private final Deck deckC = new Deck();

    private TracksIndexer tracksIndexer; private AdsIndexer adsIndexer;

    private Period currentPeriod;
    private List<Period> periods;

    public Player(Context context) {
        this.context      = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setTracksIndexer(TracksIndexer indexer) {
        this.tracksIndexer = indexer;
    }

    public void setAdsIndexer(AdsIndexer indexer) {
        this.adsIndexer = indexer;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    public void play() {
        if (!currentPeriod.isNow()) {
            stop(STATE_WAITING);
            return;
        }

        if (deckA.getAudio() == null) {
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
        } else {
            deckA.play();
        }
    }

    public void stop() {
        stop(STATE_STOPPED);

        deckA.pause();
        deckB.pause();
        deckC.pause();
    }

    protected File getDirectory() {
        return new File("sd");//context.getExternalFilesDir(null), Track.getDirectoryName());
    }

    protected Track getTrack() {
        return Track.findRandom(LibraryUtils.getTracksIndexer().getPersistedQueue(), currentPeriod.collectStyleIds());
    }

    public void stop(int state) {
        setState(state);
    }

    public int getState() {
        return currentState;
    }

    private void setState(int state) {
        this.currentState = state;
        sendBroadcast();
    }

    public void enqueue() {
        //this.currentPeriod = Period.findCurrent(place.getPeriods());

        if (currentPeriod != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, currentPeriod.getDelay(), MediaReceiver.getBroadcast(context));
        }
    }

    private void sendBroadcast() {

    }
}
/*


        Log.d(TAG, "player");
        Log.d(TAG, "Time now" + new DateTime().toString());
        Log.d(TAG, "Start" + currentPeriod.getInterval().getStart().toString());
        Log.d(TAG, "End" + currentPeriod.getInterval().getEnd().toString());
        Log.d(TAG, Long.toString((currentPeriod.getDelay() -  System.currentTimeMillis()) / 1000 / 60) );

 */