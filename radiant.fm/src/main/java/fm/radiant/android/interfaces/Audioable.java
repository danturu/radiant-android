package fm.radiant.android.interfaces;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

import fm.radiant.android.models.Audio;

public abstract class Audioable extends Modelable {
    private static String VALUE_VERSION = "h";

    private Collection<Audio> audio;

    public String getDirname() {
        return getClass().getSimpleName().toLowerCase();
    }

    public String getFilename()  {
        return getStringId() + ".mp3";
    }

    public File getDirectory(Context context) {
        return FileUtils.getFile(context.getExternalFilesDir(null), getDirname());
    }

    public File getFile(Context context) {
        return FileUtils.getFile(getDirectory(context), getFilename());
    }

    public Audio getAudio() {
        return Iterables.find(audio, new Predicate<Audio>() {
            @Override
            public boolean apply(Audio audio) {
                return audio.getLabel().equals(VALUE_VERSION);
            }
        });
    }
}