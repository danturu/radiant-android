package fm.radiant.android.lib.syncer;

import android.content.Context;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import fm.radiant.android.models.Ad;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.models.Track;

public class AdDownload extends AbstractDownload {
    public AdDownload(Context context, Ad ad, OnProgressListener event) {
        super(context, ad, event);
    }
}
