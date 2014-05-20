package fm.radiant.android.interfaces;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.Collection;

import fm.radiant.android.models.Audio;

public abstract class Audioable extends Modelable {
    private Collection<Audio> audio;

    public File getFile(File directory) {
        return new File(directory, getFilename());
    }

    public String getFilename()  {
        return new String(Hex.encodeHex(DigestUtils.sha1(getStringId()))) + ".mp3";
    }

    public int getFilesize() {
        return getAudio().getSize();
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