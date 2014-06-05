package fm.radiant.android.lib.cleaner;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

import java.io.File;
import java.util.Collection;
import java.util.List;

import fm.radiant.android.models.AudioModel;

public abstract class AbstractCleaner {
    private List<? extends AudioModel> mQueue;
    private File mDirectory;

    public AbstractCleaner(List<? extends AudioModel> queue, File directory) {
        this.mQueue     = queue;
        this.mDirectory = directory;
    }

    public void clean() {
        if (!mDirectory.isDirectory()) return;

        for (File expiredFile : getExpiredFiles()) { FileUtils.deleteQuietly(expiredFile); }
    }

    protected Collection<File> getExpiredFiles() {
        IOFileFilter filter = new NotFileFilter(new NameFileFilter(queueToFilenames()));

        return FileUtils.listFiles(mDirectory, filter, null);
    }

    protected List<String> queueToFilenames() {
        Iterable<String> filenames = Iterables.transform(mQueue, new Function<AudioModel, String>() {
            @Override
            public String apply(AudioModel model) {
                return model.getFilename();
            }
        });

        return Lists.newArrayList(filenames);
    }
}