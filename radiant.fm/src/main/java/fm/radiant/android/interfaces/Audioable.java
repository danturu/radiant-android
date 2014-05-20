package fm.radiant.android.interfaces;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.io.File;
import java.util.Collection;

import fm.radiant.android.models.Audio;

public abstract class Audioable extends Modelable {
    private Collection<Audio> audio;

    public File getFile(File directory) {
        return new File(directory, getFilename());
    }

    public String getFilename()  {
        return getStringId() + ".mp3";
    }

    public Audio getAudio() {
        return Iterables.find(audio, new Predicate<Audio>() {
            @Override
            public boolean apply(Audio audio) {
                return audio.getLabel().equals("h");
            }
        });
    }
}