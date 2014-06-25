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

    public static class PlaceCachedEvent {
    }

    public static class PlaceUnpairedEvent {
    }

    public static class SyncerStateChanged {
        private int mState;

        public SyncerStateChanged(int state) {
            mState = state;
        }

        public int getState() {
            return mState;
        }
    }

    public static class SyncerSyncedPercentChanged {
        private byte mSyncedPercent;

        public SyncerSyncedPercentChanged(byte syncedPercent) {
            mSyncedPercent = syncedPercent;
        }

        public Byte getSyncedPercent() {
            return mSyncedPercent;
        }
    }

    public static class SyncerProgressChanged {
        private long mEstimatedTime;
        private long mDownloadSpeed;


        public Long getEstimatedTime() {
            return mEstimatedTime;
        }
        public Long getDownloadSpeed() {
            return mDownloadSpeed;
        }

        public void setEstimatedTime(long estimatedTime) {
            mEstimatedTime = estimatedTime;
        }

        public void setDownloadSpeed(long downloadSpeed) {
            mDownloadSpeed = downloadSpeed;
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
