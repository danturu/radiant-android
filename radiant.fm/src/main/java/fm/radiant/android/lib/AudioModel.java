package fm.radiant.android.lib;

import android.content.Context;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.annotations.Expose;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import fm.radiant.android.models.Audio;

public abstract class AudioModel extends Model {
    private static String VALUE_VERSION   = "h";
    private static String VALUE_EXTENSION = "mp3";

    @Expose
    private Collection<fm.radiant.android.models.Audio> audio = Collections.emptyList();

    public String getDirname() {
        return getClass().getSimpleName().toLowerCase();
    }

    public String getFilename()  {
        return getStringId() + "." + VALUE_EXTENSION;
    }

    public File getDirectory(Context context) {
        return FileUtils.getFile(context.getExternalFilesDir(null), getDirname());
    }

    public File getFile(Context context) {
        return FileUtils.getFile(getDirectory(context), getFilename());
    }

    public String getSource(Context context) {
        return getFile(context).exists() ? getFile(context).getAbsolutePath() : getAudio().getURL();
    }

    public Audio getAudio() {
        return Iterables.find(audio, new Predicate<fm.radiant.android.models.Audio>() {
            @Override
            public boolean apply(fm.radiant.android.models.Audio audio) {
                return audio.getLabel().equals(VALUE_VERSION);
            }
        });
    }
}