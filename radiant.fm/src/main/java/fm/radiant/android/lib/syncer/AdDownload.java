package fm.radiant.android.lib.syncer;

import android.content.Context;

import fm.radiant.android.models.Ad;

public class AdDownload extends AbstractDownload {
    public AdDownload(Context context, Ad ad, OnProgressListener event) {
        super(context, ad, event);
    }
}
