package fm.radiant.android.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import fm.radiant.android.classes.cleaner.AbstractCleaner;

public class CleanTask extends AsyncTask<AbstractCleaner, Void, Void> {
    private static final String TAG = "CleanTask";

    private Context context;

    public CleanTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(AbstractCleaner... cleaners) {
        for (AbstractCleaner cleaner : cleaners) {
            cleaner.clean();
        }

        Log.i(TAG, "Library successfully cleaned.");

        return null;
    }
}
