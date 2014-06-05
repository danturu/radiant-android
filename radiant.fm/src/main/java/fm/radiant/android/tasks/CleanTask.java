package fm.radiant.android.tasks;

import android.os.AsyncTask;
import android.util.Log;

import fm.radiant.android.lib.cleaner.AbstractCleaner;

public class CleanTask extends AsyncTask<AbstractCleaner, Void, Void> {
    private static final String TAG = CleanTask.class.getSimpleName();

    @Override
    protected Void doInBackground(AbstractCleaner... cleaners) {
        for (AbstractCleaner cleaner : cleaners) { cleaner.clean(); }

        Log.i(TAG, "Library cleaned.");

        return null;
    }
}
