package fm.radiant.android;

import fm.radiant.android.models.Period;
import fm.radiant.android.models.Place;

public class Events {
    public static class PlaceChangedEvent {
        private Place mPlace;

        public PlaceChangedEvent(Place place) {
            mPlace = place;
        }

        public Place getPlace() {
            return mPlace;
        }
    }

    public static class PlaceUnpairedEvent {
    }

    public static class SyncerStateChanged {
        private int mState;
        private int mError;

        public SyncerStateChanged(int state, int error) {
            mState = state;
        }

        public int getState() {
            return mState;
        }

        public int getError() {
            return mError;
        }
    }

    public static class SyncerProgressChanged {
        private byte mSyncedPercent;
        private long mEstimatedTime;
        private long mDownloadSpeed;

        public SyncerProgressChanged(byte syncedPercent, long downloadSpeed, long estimatedTime) {
            mSyncedPercent = syncedPercent;
            mDownloadSpeed = downloadSpeed;
            mEstimatedTime = estimatedTime;
        }

        public Byte getSyncedPercent() {
            return mSyncedPercent;
        }

        public Long getDownloadSpeed() {
            return mDownloadSpeed;
        }

        public Long getEstimatedTime() {
            return mEstimatedTime;
        }
    }

    public static class PlayerStateChanged {
        private int mState;

        public PlayerStateChanged(int state) {
            mState = state;
        }

        public int getState() {
            return mState;
        }
    }

    public static class PlayerVolumeChanged {
        private int mVolume;

        public PlayerVolumeChanged(int volume) {
            mVolume = volume;
        }

        public int getVolume() {
            return mVolume;
        }
    }

    public static class PlayerPeriodChanged {
        private Period mPeriod;

        public PlayerPeriodChanged(Period period) {
            mPeriod = period;
        }

        public Period getPeriod() {
            return mPeriod;
        }
    }
}
